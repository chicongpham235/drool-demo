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

import jakarta.annotation.Nullable;

@Configuration
public class DroolsConfig extends BaseClass {

    private static final KieServices kieServices = KieServices.Factory.get();

    @Value("${drools.fee-cal.drl-path}")
    private String drlPath;

    @Bean(name = "kieContainer")
    @Nullable
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

        if (kb.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
            kb.getResults().getMessages(org.kie.api.builder.Message.Level.ERROR)
                    .forEach(msg -> logger.error("❌ DRL Build Error: {}", msg));
            logger.warn("⚠️ Drools KieBuilder has errors. Skipping KieContainer init.");
            return null; // hoặc throw soft exception nếu muốn fail mềm
        }

        KieModule kieModule = kb.getKieModule();
        KieContainer kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
        return kieContainer;
    }
}
