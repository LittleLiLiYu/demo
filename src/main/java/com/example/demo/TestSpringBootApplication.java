package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.demo.listener.DemoContextInitializedApplicationListener;
import com.example.demo.listener.DemoEnvironmentPreparedApplicationListener;
import com.example.demo.listener.DemoFailedEventApplicationListener;
import com.example.demo.listener.DemoPreparedApplicationListener;
import com.example.demo.listener.DemoReadyApplicationListener;
import com.example.demo.listener.DemoStartedApplicationListener;
import com.example.demo.listener.DemoStartingApplicationListener;

@SpringBootApplication
public class TestSpringBootApplication {

	public static void main(String[] args) {
	    SpringApplication app = new SpringApplication(TestSpringBootApplication.class);
	    app.addListeners(new DemoStartingApplicationListener());
	    app.addListeners(new DemoStartedApplicationListener());
	    app.addListeners(new DemoReadyApplicationListener());
	    app.addListeners(new DemoPreparedApplicationListener());
	    app.addListeners(new DemoFailedEventApplicationListener());
	    app.addListeners(new DemoEnvironmentPreparedApplicationListener());
	    app.addListeners(new DemoContextInitializedApplicationListener());
	    app.run(args);
	}

}
