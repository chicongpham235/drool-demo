package com.drools.rule_management.dto.drool;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConditionDTO {

    @NotNull
    @NotBlank
    private String field;

    @NotNull
    @NotBlank
    private String operator;

    @NotNull
    @NotBlank
    private String type;

    @NotNull
    @NotBlank
    private String value;
}
