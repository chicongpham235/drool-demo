package com.drools.rule_management.dto.drool;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConditionDTO {
    private String field;
    private String operator;
    private String value;
}
