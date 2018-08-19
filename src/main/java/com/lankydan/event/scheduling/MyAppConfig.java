//package com.lankydan.event.scheduling;
//
//import java.util.Date;
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.env.Environment;
//import org.springframework.scheduling.Trigger;
//import org.springframework.scheduling.TriggerContext;
//import org.springframework.scheduling.annotation.SchedulingConfigurer;
//import org.springframework.scheduling.config.ScheduledTaskRegistrar;
//import org.springframework.scheduling.support.CronTrigger;
//
//@Configuration
//public class MyAppConfig implements SchedulingConfigurer {
//
//    @Autowired
//    Environment env;
//    
//    @Autowired
//    MyScheduledTask myScheduledTask;
//
//    @Bean(destroyMethod = "shutdown")
//    public Executor taskExecutor() {
//        return Executors.newScheduledThreadPool(100);
//    }
//
////    @Override
////    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
////    	
////    	System.out.println("inside config task ");
////        taskRegistrar.setScheduler(taskExecutor());
////        taskRegistrar.addTriggerTask(
////                new Runnable() {
////                    @Override public void run() {
////                        System.err.println("  Hello World! " + new Date());
////                    }
////                },
////                new Trigger() {
////                    @Override public Date nextExecutionTime(TriggerContext triggerContext) {
////                        Calendar nextExecutionTime =  new GregorianCalendar();
////                        Date lastActualExecutionTime = triggerContext.lastActualExecutionTime();
////                        nextExecutionTime.setTime(lastActualExecutionTime != null ? lastActualExecutionTime : new Date());
////                        nextExecutionTime.add(Calendar.SECOND, schedularJobRepository.findOne(1).getRate()); //you can get the value from wherever you want
////                        return nextExecutionTime.getTime();
////                    }
////                }
////        );
////        
////    }
//    
//    
//    private String cronConfig() {
////        return "0 4 22 * * ?";
//    	return "*/1 * * * * ?";
//    }
//    
//
//  @Override
//  public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
//  	
//  	System.out.println("inside config task ");
//  	
//      taskRegistrar.addTriggerTask(new Runnable() {
//          @Override
//          public void run() {
//        	  System.err.println("  Hello World! " + new Date());
//        	    
////        	  myScheduledTask.doJob();
//          }
//      }, new Trigger() {
//          @Override
//          public Date nextExecutionTime(TriggerContext triggerContext) {
//              String cron = cronConfig();
//  
//              System.out.println("new cron job "+cron);
//          
//              CronTrigger trigger = new CronTrigger(cron);
//              Date nextExec = trigger.nextExecutionTime(triggerContext);
//              return nextExec;
//          }
//      });
//      
//  }
//    
//    
//    
//}