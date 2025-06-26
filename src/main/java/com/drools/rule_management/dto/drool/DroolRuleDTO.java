package com.drools.rule_management.dto.drool;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DroolRuleDTO {
    private String name;
    private Integer salience;
    private List<WhenGroupDTO> when;
    private List<ThenActionDTO> then;
}
