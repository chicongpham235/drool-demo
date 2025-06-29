package com.drools.rule_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.drools.rule_management.dto.drool.DroolRuleDTO;
import com.drools.rule_management.service.RuleManagementService;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/rule-management")
@CrossOrigin(origins = "*")
public class RuleManagementController {

    @Autowired
    RuleManagementService ruleManagementService;

    @PostMapping("")
    public ResponseEntity<?> upload(@RequestBody @Valid @NotEmpty @NotNull List<@NotNull DroolRuleDTO> rules) {
        Boolean isUploadSuccess = ruleManagementService.upload(rules);
        return ResponseEntity.ok(isUploadSuccess);
    }

}
