package com.lankydan.event.scheduling.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import com.lankydan.event.repository.SchedularJobRepository;

@Configuration
public class MyScheduledAnnotationBeanPostProcessor implements MergedBeanDefinitionPostProcessor, DestructionAwareBeanPostProcessor, DisposableBean, SmartInitializingSingleton, BeanFactoryAware, ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

	protected final Log logger = LogFactory.getLog(getClass());

	private BeanFactory beanFactory;

	private final Set<Class<?>> nonAnnotatedClasses =
			Collections.newSetFromMap(new ConcurrentHashMap<Class<?>, Boolean>(64));
	private ApplicationContext applicationContext;

	private StringValueResolver embeddedValueResolver;

	private final Map<Object, Set<ScheduledTask>> scheduledTasks =
			new IdentityHashMap<Object, Set<ScheduledTask>>(16);
	
	private final ScheduledTaskRegistrar registrar = new ScheduledTaskRegistrar();
	
	@Autowired
	SchedularJobRepository schedularJobRepository;
	
	public ScheduledTaskRegistrar getRegistrar() {
		return registrar;
	}

	@Override
	public void afterSingletonsInstantiated() {
		System.err.println("afterSingletonsInstantiated");
		// Remove resolved singleton classes from cache
		this.nonAnnotatedClasses.clear();

		if (this.applicationContext == null) {
			// Not running in an ApplicationContext -> register tasks early...
			finishRegistration();
		}
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		System.err.println("onApplicationEvent");
		if (event.getApplicationContext() == this.applicationContext) {
			// Running in an ApplicationContext -> register tasks this late...
			// giving other ContextRefreshedEvent listeners a chance to perform
			// their work at the same time (e.g. Spring Batch's job registration).
			finishRegistration();
		}
	}
	
	private void finishRegistration() {
		System.err.println("finishRegistration");
		if (this.beanFactory instanceof ListableBeanFactory) {
			Map<String, SchedulingConfigurer> beans =
					((ListableBeanFactory) this.beanFactory).getBeansOfType(SchedulingConfigurer.class);
			List<SchedulingConfigurer> configurers = new ArrayList<SchedulingConfigurer>(beans.values());
			AnnotationAwareOrderComparator.sort(configurers);
			for (SchedulingConfigurer configurer : configurers) {
				configurer.configureTasks(this.registrar);
			}
		}
		this.registrar.afterPropertiesSet();
	}
	
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		System.err.println("setBeanFactory");
		this.beanFactory = beanFactory;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		System.err.println("setApplicationContext");
		if (this.beanFactory == null) {
			this.beanFactory = applicationContext;
		}
	}
	
	@Override
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
		System.err.println("postProcessMergedBeanDefinition");
	}
	
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		System.err.println("postProcessBeforeInitialization");
		return bean;
	}
	
	@Override
	public Object postProcessAfterInitialization(final Object bean, String beanName) {
		System.err.println("postProcessAfterInitialization");
		Class<?> targetClass = AopUtils.getTargetClass(bean);
		if (!this.nonAnnotatedClasses.contains(targetClass)) {
			Map<Method, Set<MyScheduler>> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
					new MethodIntrospector.MetadataLookup<Set<MyScheduler>>() {
						@Override
						public Set<MyScheduler> inspect(Method method) {
							Set<MyScheduler> scheduledMethods = AnnotatedElementUtils.getMergedRepeatableAnnotations(
									method, MyScheduler.class);
							return (!scheduledMethods.isEmpty() ? scheduledMethods : null);
						}
					});
			if (annotatedMethods.isEmpty()) {
				this.nonAnnotatedClasses.add(targetClass);
				if (logger.isTraceEnabled()) {
					logger.trace("No @MyScheduler annotations found on bean class: " + bean.getClass());
				}
			}
			else {
				// Non-empty set of methods
				for (Map.Entry<Method, Set<MyScheduler>> entry : annotatedMethods.entrySet()) {
					Method method = entry.getKey();
					for (MyScheduler scheduled : entry.getValue()) {
						processScheduled(scheduled, method, bean);
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug(annotatedMethods.size() + " @MyScheduler methods processed on bean '" + beanName +
							"': " + annotatedMethods);
				}
			}
		}
		return bean;
	}
	
	protected void processScheduled(MyScheduler scheduled, Method method, Object bean) {
		System.err.println("processScheduled");
		try {
			Assert.isTrue(method.getParameterTypes().length == 0,
					"Only no-arg methods may be annotated with @Scheduled");

			Method invocableMethod = AopUtils.selectInvocableMethod(method, bean.getClass());
			Runnable runnable = new ScheduledMethodRunnable(bean, invocableMethod);
			boolean processedSchedule = false;
			String errorMessage =
					"Exactly one of the 'cron', 'fixedDelay(String)', or 'fixedRate(String)' attributes is required";

			Set<ScheduledTask> tasks = new LinkedHashSet<ScheduledTask>(4);

//			String cron = scheduled.cron();
//			String cron = "*/1 * * * * ?";
//			if (StringUtils.hasText(cron)) {
//				Assert.isTrue(initialDelay == -1, "'initialDelay' not supported for cron triggers");
				processedSchedule = true;
				String zone = scheduled.zone();
				if (this.embeddedValueResolver != null) {
//					cron = this.embeddedValueResolver.resolveStringValue(cron);
					zone = this.embeddedValueResolver.resolveStringValue(zone);
				}
				TimeZone timeZone;
//				String defCron = cron;
				if (StringUtils.hasText(zone)) {
					timeZone = StringUtils.parseTimeZoneString(zone);
				}
				else {
					timeZone = TimeZone.getDefault();
				}
				//tasks.add(this.registrar.scheduleCronTask(new CronTask(runnable, new CronTrigger(cron, timeZone))));
				tasks.add(this.registrar.scheduleTriggerTask(new TriggerTask(runnable, new Trigger() {
		             public Date nextExecutionTime(TriggerContext triggerContext) {
		                
		                Date nextExec = new CronSequenceGenerator(schedularJobRepository.findOne(1l).getRate(), timeZone).next(new Date());
		                
		                return nextExec;
		            }
		        })));
//			}

			// Check whether we had any attribute set
			Assert.isTrue(processedSchedule, errorMessage);

			// Finally register the scheduled tasks
			synchronized (this.scheduledTasks) {
				Set<ScheduledTask> registeredTasks = this.scheduledTasks.get(bean);
				if (registeredTasks == null) {
					registeredTasks = new LinkedHashSet<ScheduledTask>(4);
					this.scheduledTasks.put(bean, registeredTasks);
				}
				registeredTasks.addAll(tasks);
			}
			
		}
		catch (IllegalArgumentException ex) {
			throw new IllegalStateException(
					"Encountered invalid @Scheduled method '" + method.getName() + "': " + ex.getMessage());
		}
	}
	
	@Override
	public void postProcessBeforeDestruction(Object bean, String beanName) {
		System.err.println("postProcessBeforeDestruction");
		Set<ScheduledTask> tasks;
		synchronized (this.scheduledTasks) {
			tasks = this.scheduledTasks.remove(bean);
		}
		if (tasks != null) {
			for (ScheduledTask task : tasks) {
				task.cancel();
			}
		}
	}

	@Override
	public boolean requiresDestruction(Object bean) {
		System.err.println("requiresDestruction");
		synchronized (this.scheduledTasks) {
			return this.scheduledTasks.containsKey(bean);
		}
	}

	@Override
	public void destroy() {
		System.err.println("destroy");
		synchronized (this.scheduledTasks) {
			Collection<Set<ScheduledTask>> allTasks = this.scheduledTasks.values();
			for (Set<ScheduledTask> tasks : allTasks) {
				for (ScheduledTask task : tasks) {
					task.cancel();
				}
			}
			this.scheduledTasks.clear();
		}
		this.registrar.destroy();
	}
}
