//package com.lankydan.event.scheduling;
//
//import java.util.Date;
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledFuture;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.TaskScheduler;
//import org.springframework.scheduling.Trigger;
//import org.springframework.scheduling.TriggerContext;
//import org.springframework.scheduling.annotation.SchedulingConfigurer;
//import org.springframework.scheduling.config.ScheduledTaskRegistrar;
//import org.springframework.scheduling.support.CronTrigger;
//
//@Configuration
//public class CronConfig implements SchedulingConfigurer{
//
//    @Autowired
//    private ScheduledFuture<?> future;
//
//    @Autowired
//    private TaskScheduler scheduler;
//
//    @Bean(destroyMethod = "shutdown")
//    public Executor taskExecutor() {
//        return Executors.newScheduledThreadPool(100);
//    } 
//
//     
//    public void start() {
//        future = scheduler.schedule(new Runnable() {
//            public void run() {
//                System.err.println("  Hello World! " + new Date());
//            }
//        }, new Trigger() {
//             public Date nextExecutionTime(TriggerContext triggerContext) {
//                
//                CronTrigger trigger = new CronTrigger("*/1 * * * * ?");
//                Date nextExec = trigger.nextExecutionTime(triggerContext);
//                
//                return nextExec;
//            }
//        });
//
//    }
//
//
//    public void stop() {
//        future.cancel(true);
//
//    }
//
//    @Override
//    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
//        start();
//    }
//
//}