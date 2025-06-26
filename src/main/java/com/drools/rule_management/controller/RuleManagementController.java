package com.drools.rule_management.controller;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.drools.rule_management.base.BaseClass;
import com.drools.rule_management.dto.drool.DroolRuleDTO;
import com.drools.rule_management.enums.ChannelTransaction;
import com.drools.rule_management.enums.TierCustomer;
import com.drools.rule_management.enums.TypeCustomer;
import com.drools.rule_management.enums.TypeTransaction;
import com.drools.rule_management.model.Customer;
import com.drools.rule_management.model.Transaction;
import com.drools.rule_management.service.RuleManagementService;

import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/drools-rule")
@CrossOrigin(origins = "*")
public class RuleManagementController extends BaseClass {

    @Autowired
    public RuleManagementController() {
        super.getInstance(RuleManagementController.class);
    }

    @Autowired
    private KieContainer kieContainer;

    @Autowired
    RuleManagementService ruleManagementService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@Valid @RequestBody List<DroolRuleDTO> rules) {
        Boolean isUploadSuccess = ruleManagementService.upload(rules);
        return ResponseEntity.ok(isUploadSuccess);
    }

    @PostMapping("/test")
    public Object testDrool() {
        Transaction transaction = new Transaction();
        transaction.setAmount(100000000.00);
        transaction.setChannel(ChannelTransaction.ONLINE.toString());
        transaction.setType(TypeTransaction.TRANSFER.toString());

        Customer customer = new Customer();
        customer.setName("PCC");
        customer.setTier(TierCustomer.DIAMOND.toString());
        customer.setType(TypeCustomer.CORPORATE.toString());

        KieSession kieSession = kieContainer.newKieSession();
        kieSession.insert(transaction);
        kieSession.insert(customer);
        kieSession.fireAllRules();
        kieSession.dispose();
        return transaction.getFee();
    }

}
