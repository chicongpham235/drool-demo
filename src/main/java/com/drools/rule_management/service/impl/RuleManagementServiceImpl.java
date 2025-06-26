package com.drools.rule_management.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.drools.rule_management.dto.drool.DroolRuleDTO;
import com.drools.rule_management.service.RuleManagementService;
import com.drools.rule_management.utils.RuleDroolsUtils;

@Service
public class RuleManagementServiceImpl implements RuleManagementService {

    @Value("${drools.fee-cal.drl-path}")
    private String drlPath;

    @Override
    public Boolean upload(List<DroolRuleDTO> rules) {
        File file = new File(drlPath);
        try (FileWriter writer = new FileWriter(file, false)) {
            String rulesDRL = RuleDroolsUtils.convert(rules);
            writer.write(rulesDRL);
            writer.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
