package com.example.demo.listener;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

/**
 * <p>Title: DemoReadyApplicationListener</p>
 * <p>Description: </p>
 * <p>Company: sunline</p>
 * @author Kuangyu
 * @date 2019年3月6日
 * @version 1.0
 */
public class DemoReadyApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        System.out.println("DemoReadyApplicationListener自定义ready");
    }

}
