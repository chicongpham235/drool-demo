package com.drools.rule_management.model;

import com.drools.rule_management.enums.ChannelTransaction;
import com.drools.rule_management.enums.TypeTransaction;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Transaction {
    private Double amount;
    private TypeTransaction type;
    private ChannelTransaction channel;
    private Double fee;
}
