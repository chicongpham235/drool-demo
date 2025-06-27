package com.drools.rule_management.helper;

import com.drools.rule_management.dto.drool.ConditionDTO;
import com.drools.rule_management.dto.drool.DroolRuleDTO;
import com.drools.rule_management.dto.drool.ThenActionDTO;
import com.drools.rule_management.dto.drool.WhenGroupDTO;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Sử dụng DRLDocument để lưu DRL rule tạm thời trong memory thay vì
 * StringBuilder thuần.
 */
@Component
public class DRLHelper {

    @Value("${drools.fee-cal.variables.globals.flag-name}")
    private String globalFlagName;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DRLHelper.class);

    private DRLDocument drl;

    public DRLDocument createDRL(List<DroolRuleDTO> rules) {
        drl = new DRLDocument();
        try {
            drl.appendLine("global java.util.concurrent.atomic.AtomicBoolean " + globalFlagName + ";\n");
            for (DroolRuleDTO rule : rules) {
                drl.appendLine("rule \"" + rule.getName() + "\"");
                if (rule.getSalience() != null) {
                    drl.appendLine("    salience " + rule.getSalience());
                }
                drl.appendLine("when");
                drl.appendLine("    eval(" + globalFlagName + ".get() == false)");

                // Mapping: Map object name to variable name (for references)
                Map<String, String> objectVarMap = new HashMap<>();
                char varSeq = 'a';

                List<WhenGroupDTO> whenGroups = rule.getWhen();
                if (whenGroups == null || whenGroups.isEmpty()) {
                    for (ThenActionDTO action : rule.getThen()) {
                        if (action.getObject() != null) {
                            String object = action.getObject();
                            String var = object.substring(0, 1).toLowerCase() + (varSeq++);
                            objectVarMap.put(object, var);
                            drl.appendLine("    $" + var + " : " + object + "()");
                        }
                    }
                } else {
                    for (WhenGroupDTO whenGroup : whenGroups) {
                        String object = whenGroup.getObject();
                        String var = object.substring(0, 1).toLowerCase() + (varSeq++);
                        objectVarMap.put(object, var);

                        StringBuilder line = new StringBuilder("    $" + var + " : " + object);

                        List<String> constraints = new ArrayList<>();
                        if (whenGroup.getConditions() != null && !whenGroup.getConditions().isEmpty()) {
                            for (ConditionDTO cond : whenGroup.getConditions()) {
                                String expr = "";
                                if (cond.getValue() != null) {
                                    String value = cond.getValue();
                                    if (value.matches("-?\\d+(\\.\\d+)?")) {
                                        expr = cond.getField() + " " + cond.getOperator() + " " + value;
                                    } else {
                                        expr = cond.getField() + " " + cond.getOperator() + " \"" + value + "\"";
                                    }
                                }
                                constraints.add(expr);
                            }
                        } else {
                            constraints.add("");
                        }

                        if (!constraints.isEmpty()) {
                            line.append("(").append(String.join(", ", constraints)).append(")");
                        }
                        drl.appendLine(line.toString());
                    }
                }

                drl.appendLine("then");
                drl.appendLine("    " + globalFlagName + ".set(true);");
                if (rule.getThen() != null) {
                    for (ThenActionDTO action : rule.getThen()) {
                        String obj = action.getObject();
                        String var = objectVarMap.getOrDefault(obj, obj.substring(0, 1).toLowerCase());
                        String valueExpressionStr = "null";
                        if (action.getValue() != null) {
                            valueExpressionStr = replaceFieldReferences(action.getValue(), objectVarMap);
                        }
                        drl.appendLine("    $" + var + "." + action.getAction() + "(" + valueExpressionStr + ");");
                    }
                }
                for (Map.Entry<String, String> entry : objectVarMap.entrySet()) {
                    String obj = entry.getKey();
                    String var = entry.getValue();
                    Boolean existKeyInThen = rule.getThen().stream()
                            .anyMatch(action -> action.getObject() != null && action.getObject().equals(obj));
                    if (existKeyInThen)
                        drl.appendLine("    update($" + var + ");");
                }
                drl.appendLine("end\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error creating DRL: " + e.getMessage());
            drl.clear();
            drl = null;
        }
        return drl;
    }

    // Helper to replace possible object.field references in params with
    // $<var>.get<Field>()
    private static String replaceFieldReferences(String expr, Map<String, String> objectVarMap) {
        if (expr == null)
            return "";

        for (Map.Entry<String, String> entry : objectVarMap.entrySet()) {
            String obj = entry.getKey();
            String var = entry.getValue();

            // Tìm tất cả field được dùng trong biểu thức
            Pattern fieldPattern = Pattern.compile("\\b(" + obj + "\\.)?([a-zA-Z_][a-zA-Z0-9_]*)\\b");
            Matcher matcher = fieldPattern.matcher(expr);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String field = matcher.group(2);
                String replacement = "\\$" + var + ".get" + capitalize(field) + "()";
                matcher.appendReplacement(sb, replacement);
            }
            matcher.appendTail(sb);
            expr = sb.toString();
        }

        return expr;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty())
            return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}