package com.rit.researchflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.rit.researchflow.config.RequiredDbEnvInitializer;

@SpringBootApplication
public class ResearchFlowApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ResearchFlowApplication.class);
        application.addInitializers(new RequiredDbEnvInitializer());
        application.run(args);
    }
}
