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

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RuleManagementService.class);

    @Value("${drools.fee-cal.drl-path}")
    private String drlPath;

    @Autowired
    MQTTv5ProducerService mqttService;

    @Autowired
    DRLHelper drlHelper;

    @Override
    public Boolean upload(List<DroolRuleDTO> rules) {
        File file = new File(drlPath);
        Boolean isUploadSuccess = false;
        try (FileWriter writer = new FileWriter(file, false)) {
            DRLDocument rulesDRL = drlHelper.createDRL(rules);
            rulesDRL.save(drlPath);
            isUploadSuccess = true;
        } catch (Exception e) {
            logger.error("Error while uploading rules: " + e.getMessage());
        }
        if (isUploadSuccess) {
            mqttService.send(new DomainEvent("create", "success"));
        }
        return isUploadSuccess;
    }
}
