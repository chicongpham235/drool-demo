package com.drools.rule_management.dto.drool;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThenActionDTO {
    private String object;
    private String action;
    private List<String> params;
}