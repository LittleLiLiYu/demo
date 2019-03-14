package com.example.demo.listener;

import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.ApplicationListener;

/**
 * <p>Title: DemoContextInitializedApplicationListener</p>
 * <p>Description: </p>
 * <p>Company: sunline</p>
 * @author Kuangyu
 * @date 2019年3月6日
 * @version 1.0
 */
public class DemoContextInitializedApplicationListener implements ApplicationListener<ApplicationContextInitializedEvent> {

    @Override
    public void onApplicationEvent(ApplicationContextInitializedEvent event) {
        System.out.println("DemoContextInitializedApplicationListener自定义ContextInitialized");          
    }

}
