package com.drools.rule_management.producer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.json.JSONObject;

import com.drools.rule_management.module.DomainEvent;
import com.drools.rule_management.module.IDomainEvent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DroolRuleSocketDetail implements IDomainEvent {
    private UUID id;
    private String code;
    private String status;

    public DroolRuleSocketDetail(DomainEvent domainEvent) {
        this.id = domainEvent.getId();
        this.code = domainEvent.getCode();
        this.status = domainEvent.getStatus();
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("code", code);
        jsonObject.put("status", status);
        return jsonObject;
    }

    public byte[] toBytes() {
        return toJSONObject().toString().getBytes(StandardCharsets.UTF_8);
    }
}
