package com.drools.rule_management.dto.drool;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WhenGroupDTO {

    @NotNull
    @NotBlank
    private String object;

    @NotNull
    @NotEmpty
    @Valid
    private List<ConditionDTO> conditions;
}
