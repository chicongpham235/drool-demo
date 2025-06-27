package com.drools.rule_management.module;

import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DomainEvent implements IDomainEvent {
    private UUID id = UUID.randomUUID();
    private String code;
    private String status;

    public DomainEvent(String code, String status) {
        this.code = code;
        this.status = status;
    }

}
