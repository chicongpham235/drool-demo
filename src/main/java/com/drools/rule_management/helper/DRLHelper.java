package com.drools.rule_management.helper;

import com.drools.rule_management.dto.drool.ConditionDTO;
import com.drools.rule_management.dto.drool.DroolRuleDTO;
import com.drools.rule_management.dto.drool.ThenActionDTO;
import com.drools.rule_management.dto.drool.WhenGroupDTO;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Sử dụng DRLDocument để lưu DRL rule tạm thời trong memory thay vì
 * StringBuilder thuần.
 */
public class DRLHelper {

    private static DRLDocument drl;

    public static void init() {
        drl = new DRLDocument();
    }

    public static DRLDocument getDrl() {
        if (drl == null) {
            init();
        }
        return drl;
    }

    public static DRLDocument createDRL(List<DroolRuleDTO> rules) {
        DRLDocument drl = getDrl();
        // drl.appendLine("package rules;");
        // drl.appendLine("import ...");

        for (DroolRuleDTO rule : rules) {
            drl.appendLine("rule \"" + rule.getName() + "\"");
            if (rule.getSalience() != null) {
                drl.appendLine("    salience " + rule.getSalience());
            }
            drl.appendLine("when");

            // Mapping: Map object name to variable name (for references)
            Map<String, String> objectVarMap = new HashMap<>();
            char varSeq = 'a';

            List<WhenGroupDTO> whenGroups = rule.getWhen();
            if (whenGroups == null || whenGroups.isEmpty()) {
                String object = "Transaction";
                String var = object.substring(0, 1).toLowerCase() + (varSeq++);
                objectVarMap.put(object, var);

                drl.appendLine("    $" + var + " : " + object + "(fee == null)");
            } else {
                for (WhenGroupDTO whenGroup : whenGroups) {
                    String object = whenGroup.getObject();
                    String var = object.substring(0, 1).toLowerCase() + (varSeq++);
                    objectVarMap.put(object, var);

                    StringBuilder line = new StringBuilder("    $" + var + " : " + object);

                    List<String> constraints = new ArrayList<>();
                    if (whenGroup.getConditions() != null && !whenGroup.getConditions().isEmpty()) {
                        for (ConditionDTO cond : whenGroup.getConditions()) {
                            String expr;
                            if (cond.getValue() != null) {
                                String value = cond.getValue();
                                if (value.matches("-?\\d+(\\.\\d+)?")) {
                                    expr = cond.getField() + " " + cond.getOperator() + " " + value;
                                } else {
                                    expr = cond.getField() + " " + cond.getOperator() + " \"" + value + "\"";
                                }
                            } else {
                                expr = "fee == null";
                            }
                            constraints.add(expr);
                        }
                    } else {
                        constraints.add("fee == null");
                    }

                    if (!constraints.isEmpty()) {
                        line.append("(").append(String.join(", ", constraints)).append(")");
                    }
                    drl.appendLine(line.toString());
                }
            }

            drl.appendLine("then");
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
                    drl.appendLine("    $" + var + "." + action.getAction() + "(" + paramsStr + ");");
                }
            }
            String transactionVar = objectVarMap.get("Transaction");
            if (transactionVar != null) {
                drl.appendLine("    update($" + transactionVar + ");");
            }
            drl.appendLine("end\n");
        }
        DRLHelper.drl = null;
        return drl;
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