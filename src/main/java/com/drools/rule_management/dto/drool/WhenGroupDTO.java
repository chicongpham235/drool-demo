package com.drools.rule_management.dto.drool;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WhenGroupDTO {
    private String object;
    private List<ConditionDTO> conditions;
}
