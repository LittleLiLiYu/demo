package com.example.demo.listener;

import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;

/**
 * <p>Title: DemoPreparedApplicationListener</p>
 * <p>Description: </p>
 * <p>Company: sunline</p>
 * @author Kuangyu
 * @date 2019年3月6日
 * @version 1.0
 */
public class DemoPreparedApplicationListener implements ApplicationListener<ApplicationPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        System.out.println("DemoPreparedApplicationListener自定义Prepared");        
    }

}
