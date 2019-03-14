package com.example.demo.listener;

import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;

/**
 * <p>Title: DemoFailedEventApplicationListener</p>
 * <p>Description: </p>
 * <p>Company: sunline</p>
 * @author Kuangyu
 * @date 2019年3月6日
 * @version 1.0
 */
public class DemoFailedEventApplicationListener implements ApplicationListener<ApplicationFailedEvent> {

    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {
        System.out.println("DemoFailedEventApplicationListener自定义Failed");          
    }

}
