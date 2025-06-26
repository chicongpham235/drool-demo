package com.drools.rule_management.base;

import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseClass {
    private static BaseClass instance;
    protected static Logger logger;

    protected BaseClass() {
    }

    public static <T extends BaseClass> BaseClass getInstance(Class<T> clazz) {
        logger = LoggerFactory.getLogger(clazz);
        if (instance == null) {
            instance = new BaseClass();
        }
        return instance;
    }
}
