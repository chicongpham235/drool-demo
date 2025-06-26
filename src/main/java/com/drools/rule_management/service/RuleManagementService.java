package com.drools.rule_management.service;

import java.util.List;

import com.drools.rule_management.dto.drool.DroolRuleDTO;

public interface RuleManagementService {
    Boolean upload(List<DroolRuleDTO> rules);
}
