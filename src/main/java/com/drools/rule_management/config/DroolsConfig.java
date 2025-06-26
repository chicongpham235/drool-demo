package com.drools.rule_management.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.drools.rule_management.base.BaseClass;

@Configuration
public class DroolsConfig extends BaseClass {

    private static final KieServices kieServices = KieServices.Factory.get();

    @Value("${drools.fee-cal.drl-path}")
    private String drlPath;

    @Bean
    public KieContainer kieContainer() {
        super.getInstance(DroolsConfig.class);

        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        try {
            Resource resource = ResourceFactory.newFileResource(drlPath);
            if (resource.getInputStream() != null) {
                kieFileSystem.write(resource);
            }
        } catch (Exception e) {
            logger.warn("Warning: Rule file not found: " + drlPath);
        }
        KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
        kb.buildAll();
        KieModule kieModule = kb.getKieModule();
        KieContainer kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
        return kieContainer;
    }
}
