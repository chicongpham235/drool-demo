package com.drools.rule_management.utils;

import com.drools.rule_management.dto.drool.ConditionDTO;
import com.drools.rule_management.dto.drool.DroolRuleDTO;
import com.drools.rule_management.dto.drool.ThenActionDTO;
import com.drools.rule_management.dto.drool.WhenGroupDTO;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RuleDroolsUtils {

    public static String convert(List<DroolRuleDTO> rules) {
        StringBuilder drl = new StringBuilder();
        drl.append("package rules;\n\n");
        drl.append("import com.drools.rule_management.model.Transaction;\n");
        drl.append("import com.drools.rule_management.model.Customer;\n\n");

        for (DroolRuleDTO rule : rules) {
            drl.append("rule \"").append(rule.getName()).append("\"\n");
            if (rule.getSalience() != null) {
                drl.append("    salience ").append(rule.getSalience()).append("\n");
            }
            drl.append("when\n");

            // Mapping: Map object name to variable name (for references)
            Map<String, String> objectVarMap = new HashMap<>();
            char varSeq = 'a';

            for (WhenGroupDTO whenGroup : rule.getWhen()) {
                String object = whenGroup.getObject();
                String var = object.substring(0, 1).toLowerCase() + (varSeq++);
                objectVarMap.put(object, var);

                drl.append("    $").append(var).append(" : ").append(object);

                List<String> constraints = new ArrayList<>();
                if (whenGroup.getConditions() != null) {
                    for (ConditionDTO cond : whenGroup.getConditions()) {
                        String expr;
                        // if (cond.getValueRef() != null && !cond.getValueRef().isEmpty()) {
                        // // Parse valueRef: e.g. Transaction.customerId
                        // String[] parts = cond.getValueRef().split("\\.");
                        // String refObj = parts[0];
                        // String refField = parts[1];
                        // String refVar = objectVarMap.getOrDefault(refObj, refObj.substring(0,
                        // 1).toLowerCase());
                        // expr = cond.getField() + " " + cond.getOperator() + " $" + refVar + "." +
                        // refField;
                        // }
                        if (cond.getValue() != null) {
                            String value = cond.getValue();
                            // Check if value is a number
                            if (value.matches("-?\\d+(\\.\\d+)?")) {
                                expr = cond.getField() + " " + cond.getOperator() + " " + value;
                            } else {
                                expr = cond.getField() + " " + cond.getOperator() + " \"" + value + "\"";
                            }
                        } else {
                            expr = cond.getField() + " " + cond.getOperator();
                        }
                        constraints.add(expr);
                    }
                }

                if (!constraints.isEmpty()) {
                    drl.append("(").append(String.join(", ", constraints)).append(")");
                } else {
                    drl.append("(fee == null)");
                }

                drl.append("\n");
            }

            drl.append("then\n");
            if (rule.getThen() != null) {
                for (ThenActionDTO action : rule.getThen()) {
                    String obj = action.getObject();
                    String var = objectVarMap.getOrDefault(obj, obj.substring(0, 1).toLowerCase());
                    String paramsStr = "";
                    if (action.getParams() != null && !action.getParams().isEmpty()) {
                        paramsStr = action.getParams().stream()
                                .map(param -> replaceFieldReferences(param, objectVarMap))
                                .collect(Collectors.joining(", "));
                    }
                    drl.append("    $").append(var).append(".").append(action.getAction())
                            .append("(").append(paramsStr).append(");\n");
                }
            }
            String transactionVar = objectVarMap.get("Transaction");
            if (transactionVar != null) {
                drl.append("    update($").append(transactionVar).append(");\n");
            }
            drl.append("end\n\n");
        }
        return drl.toString();
    }

    // Helper to replace possible object.field references in params with
    // $<var>.get<Field>()
    private static String replaceFieldReferences(String expr, Map<String, String> objectVarMap) {
        // Replace Transaction.amount => $a.getAmount()
        // Replace Customer.type => $b.getType()
        if (expr == null)
            return "";
        for (Map.Entry<String, String> entry : objectVarMap.entrySet()) {
            String obj = entry.getKey();
            String var = entry.getValue();
            Pattern p = Pattern.compile(obj + "\\.([a-zA-Z_][a-zA-Z0-9_]*)");
            Matcher matcher = p.matcher(expr);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String prop = matcher.group(1);
                String getter = "\\$" + var + ".get" + prop.substring(0, 1).toUpperCase() + prop.substring(1) + "()";
                matcher.appendReplacement(sb, getter);
            }
            matcher.appendTail(sb);
            expr = sb.toString();
        }
        return expr;
    }
}
