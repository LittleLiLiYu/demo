package com.example.demo.listener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * <p>Title: DemoSpringApplicationRunListener</p>
 * <p>Description: </p>
 * <p>Company: sunline</p>
 * @author Kuangyu
 * @date 2019年3月6日
 * @version 1.0
 */
public class DemoSpringApplicationRunListener implements SpringApplicationRunListener {
    
    private final SpringApplication application;
    private final String[] args;
    public DemoSpringApplicationRunListener(SpringApplication sa, String[] args) {
        this.application = sa;
        this.args = args;
    }

    public SpringApplication getApplication() {
        return application;
    }

    public String[] getArgs() {
        return args;
    }

    @Override
    public void starting() {
        System.out.println("DemoSpringApplicationRunListener自定义starting");
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        System.out.println("DemoSpringApplicationRunListener自定义environmentPrepared");

    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        System.out.println("DemoSpringApplicationRunListener自定义contextPrepared");
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        System.out.println("DemoSpringApplicationRunListener自定义contextLoaded");
    }

    @Override
    public void started(ConfigurableApplicationContext context) {
        System.out.println("DemoSpringApplicationRunListener自定义started");
    }

    @Override
    public void running(ConfigurableApplicationContext context) {
        System.out.println("DemoSpringApplicationRunListener自定义running");
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        System.out.println("DemoSpringApplicationRunListener自定义failed");
    }

}
