package com.drools.rule_management.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.drools.rule_management.dto.drool.DroolRuleDTO;
import com.drools.rule_management.module.DomainEvent;
import com.drools.rule_management.producer.MQTTv5ProducerService;
import com.drools.rule_management.service.RuleManagementService;
import com.drools.rule_management.utils.RuleDroolsUtils;

import java.util.UUID;

@Service
public class RuleManagementServiceImpl implements RuleManagementService {

    @Value("${drools.fee-cal.drl-path}")
    private String drlPath;

    @Autowired
    MQTTv5ProducerService mqttService;

    @Override
    public Boolean upload(List<DroolRuleDTO> rules) {
        File file = new File(drlPath);
        try (FileWriter writer = new FileWriter(file, false)) {
            String rulesDRL = RuleDroolsUtils.convert(rules);
            writer.write(rulesDRL);
            writer.flush();
            mqttService.send(new DomainEvent("create", "success"));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
