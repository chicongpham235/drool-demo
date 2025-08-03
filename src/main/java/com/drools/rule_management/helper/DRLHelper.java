package com.drools.rule_management.helper;

import com.drools.rule_management.dto.drool.ConditionDTO;
import com.drools.rule_management.dto.drool.DroolRuleDTO;
import com.drools.rule_management.dto.drool.ThenActionDTO;
import com.drools.rule_management.dto.drool.WhenGroupDTO;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Sử dụng DRLDocument để lưu DRL rule tạm thời trong memory thay vì
 * StringBuilder thuần.
 */
@Component
public class DRLHelper {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DRLHelper.class);
    private static final Pattern FIELD_PATTERN = Pattern
            .compile("\\b([a-zA-Z_][a-zA-Z0-9_]*(?:\\.[a-zA-Z_][a-zA-Z0-9_]*)*)\\b");

    public DRLDocument createDRL(List<DroolRuleDTO> rules) {
        DRLDocument drl = new DRLDocument();
        try {
            for (DroolRuleDTO rule : rules) {
                drl.appendLine("rule \"" + rule.getName() + "\"");
                if (rule.getSalience() != null) {
                    drl.appendLine("    salience " + rule.getSalience());
                }
                drl.appendLine("when");

                // Mapping: Map object name to variable name (for references)
                Map<String, Map<String, Object>> objectVarMap = new HashMap<>();
                char varSeq = 'a';

                for (ThenActionDTO action : rule.getThen()) {
                    if (action.getObject() != null) {
                        String objectName = action.getObject();
                        String varName = objectName.substring(0, 1).toLowerCase() + (varSeq++);
                        Map<String, Object> value = new HashMap<>();
                        value.put("objectName", objectName);
                        value.put("varName", varName);
                        value.put("conditions", null);
                        objectVarMap.put(objectName, value);
                    }
                }

                List<WhenGroupDTO> whenGroups = rule.getWhen();
                if (whenGroups != null && !whenGroups.isEmpty()) {
                    for (WhenGroupDTO whenGroup : whenGroups) {
                        String objectName = whenGroup.getObject();
                        String varName = objectName.substring(0, 1).toLowerCase() + (varSeq++);
                        Map<String, Object> value = new HashMap<>();
                        value.put("objectName", objectName);
                        value.put("varName", varName);
                        value.put("conditions", whenGroup.getConditions());
                        objectVarMap.put(objectName, value);
                    }
                }

                for (Map.Entry<String, Map<String, Object>> entry : objectVarMap.entrySet()) {
                    String objectName = entry.getKey();
                    Map<String, Object> value = entry.getValue();

                    StringBuilder line = new StringBuilder("    $" + value.get("varName") + " : " + objectName);

                    List<String> constraints = new ArrayList<>();

                    @SuppressWarnings("unchecked")
                    List<ConditionDTO> conditions = (List<ConditionDTO>) value.get("conditions");

                    if (conditions != null && !conditions.isEmpty()) {
                        for (ConditionDTO cond : conditions) {
                            if ("API".equals(cond.getType())) {
                                continue;
                            }
                            String expr = "";
                            if (cond.getValue() != null) {
                                String condValue = cond.getValue();
                                if (condValue.matches("-?\\d+(\\.\\d+)?")) {
                                    expr = cond.getField() + " " + cond.getOperator() + " " + condValue;
                                } else {
                                    expr = cond.getField() + " " + cond.getOperator() + " \"" + condValue + "\"";
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

                drl.appendLine("then");
                List<String> apiConditions = new ArrayList<>();
                for (Map.Entry<String, Map<String, Object>> entry : objectVarMap.entrySet()) {
                    Map<String, Object> value = entry.getValue();

                    @SuppressWarnings("unchecked")
                    List<ConditionDTO> conditions = (List<ConditionDTO>) value.get("conditions");

                    if (conditions != null && !conditions.isEmpty()) {
                        for (ConditionDTO cond : conditions) {
                            if (!"API".equals(cond.getType())) {
                                continue;
                            }
                            if (cond.getValue() != null) {
                                apiConditions.add(
                                        "$" + value.get("varName") + "." + cond.getField() + " " + cond.getOperator()
                                                + " " + "callApi(\"" + cond.getValue() + "\")");
                            }
                        }
                    }
                }
                if (!apiConditions.isEmpty()) {
                    drl.appendLine("    if (" + String.join(" && ", apiConditions) + ") {");
                }
                if (rule.getThen() != null) {
                    for (ThenActionDTO action : rule.getThen()) {
                        String obj = action.getObject();
                        String var = objectVarMap.get(obj).get("varName").toString().toLowerCase();
                        String valueExpressionStr = "null";
                        if (action.getValue() != null) {
                            valueExpressionStr = replaceFieldReferences(action.getValue(), objectVarMap, rule);
                        }
                        drl.appendLine("    $" + var + "." + action.getAction() + "(" + valueExpressionStr + ");");
                    }
                }
                if (!apiConditions.isEmpty()) {
                    drl.appendLine("    }");
                }
                for (Map.Entry<String, Map<String, Object>> entry : objectVarMap.entrySet()) {
                    String objName = entry.getKey();
                    Map<String, Object> value = entry.getValue();
                    Boolean existKeyInThen = rule.getThen().stream()
                            .anyMatch(action -> action.getObject() != null && action.getObject().equals(objName));
                    if (existKeyInThen)
                        drl.appendLine("    update($" + value.get("varName") + ");");
                }
                drl.appendLine("end\n");
            }
        } catch (Exception e) {
            logger.error("Error creating DRL: " + e.getMessage());
            drl.clear();
            throw new RuntimeException("Error creating DRL: " + e.getMessage());
        }
        logger.info("DRL content created successfully");
        return drl;
    }

    // Helper to replace possible object.field references in params with
    // $<var>.get<Field>()
    private static String replaceFieldReferences(String expr, Map<String, Map<String, Object>> objectVarMap,
            DroolRuleDTO rule) {
        if (expr == null)
            return "";

        for (Map.Entry<String, Map<String, Object>> entry : objectVarMap.entrySet()) {
            String objName = entry.getKey();
            Map<String, Object> value = entry.getValue();
            Boolean existKeyInThen = rule.getThen().stream()
                    .anyMatch(action -> action.getObject() != null && action.getObject().equals(objName));
            if (!existKeyInThen)
                continue;

            // Tìm tất cả field được dùng trong biểu thức
            Matcher matcher = FIELD_PATTERN.matcher(expr);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String field = matcher.group(1);
                String replacement = "\\$" + value.get("varName") + ".get" + capitalize(field) + "()";
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