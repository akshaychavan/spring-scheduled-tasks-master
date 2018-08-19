//package com.lankydan.event.scheduling.annotation;
//
//import java.util.Date;
//
//import org.springframework.scheduling.Trigger;
//import org.springframework.scheduling.TriggerContext;
//import org.springframework.scheduling.support.CronSequenceGenerator;
//import org.springframework.scheduling.support.CronTrigger;
//
//public class MyCronTrigger implements Trigger {
//	
//	private final CronSequenceGenerator sequenceGenerator;
//
//	@Override
//	public Date nextExecutionTime(TriggerContext triggerContext) {
//        
//        CronTrigger trigger = new CronTrigger(cron, timeZone);
//        Date nextExec = trigger.nextExecutionTime(triggerContext);
//        
//        return nextExec;
//    }
//
//}
