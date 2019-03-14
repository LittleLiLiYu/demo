package com.example.demo.listener;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

/**
 * <p>Title: DemoEnvironmentPreparedApplicationListener</p>
 * <p>Description: </p>
 * <p>Company: sunline</p>
 * @author Kuangyu
 * @date 2019年3月6日
 * @version 1.0
 */
public class DemoEnvironmentPreparedApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        System.out.println("DemoEnvironmentPreparedApplicationListener自定义EnvironmentPrepared");          
    }

}
