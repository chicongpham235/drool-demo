package com.drools.rule_management.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.drools.rule_management.dto.drool.DroolRuleDTO;
import com.drools.rule_management.module.DomainEvent;
import com.drools.rule_management.producer.MQTTv5ProducerService;
import com.drools.rule_management.service.RuleManagementService;
import com.drools.rule_management.helper.DRLDocument;
import com.drools.rule_management.helper.DRLHelper;

@Service
public class RuleManagementServiceImpl implements RuleManagementService {

    @Value("${drools.fee-cal.drl-path}")
    private String drlPath;

    @Autowired
    MQTTv5ProducerService mqttService;

    @Autowired
    DRLHelper drlHelper;

    @Override
    public Boolean upload(List<DroolRuleDTO> rules) {
        File file = new File(drlPath);
        try (FileWriter writer = new FileWriter(file, false)) {
            DRLDocument rulesDRL = drlHelper.createDRL(rules);
            rulesDRL.save(drlPath);
            mqttService.send(new DomainEvent("create", "success"));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
