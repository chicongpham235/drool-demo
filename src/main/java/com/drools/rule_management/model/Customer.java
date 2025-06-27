package com.drools.rule_management.model;

import com.drools.rule_management.enums.TierCustomer;
import com.drools.rule_management.enums.TypeCustomer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Customer {
    private String name;
    private TypeCustomer type;
    private TierCustomer tier;
}
