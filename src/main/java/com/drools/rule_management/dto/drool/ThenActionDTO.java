package com.drools.rule_management.dto.drool;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThenActionDTO {

    @NotNull
    @NotBlank
    private String object;

    @NotNull
    @NotBlank
    private String action;

    @NotNull
    @NotBlank
    private String value;
}