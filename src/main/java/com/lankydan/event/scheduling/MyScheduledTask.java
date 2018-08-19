package com.lankydan.event.scheduling;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.lankydan.event.scheduling.annotation.MyScheduler;

@Component
public class MyScheduledTask {
	

 	private static final Logger log = LoggerFactory.getLogger(MyScheduledTask.class);


	
// 	@Scheduled(cron = "0 14 19 * * ?")
//    public void doJobOld() {

// //		myScheduledAnnotationBeanPostProcessor.getRegistrar().getTriggerTaskList().forEach(x -> {
// //			log.info(x.toString());
// //		});

   
//    }
	
	@MyScheduler
    public void doJob() {

		log.info(" doJob " + new Date());
    
    }
	
		@MyScheduler
    public void doJob1() {

		log.info(" doJob new " + new Date());
    
    }

}
