package com.alibaba.csp.sentinel.dashboard.util;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AbstractRuleEntity;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;

public class RegexRuleUtil {



    public static final String REGEX_STRING = ".*";



    public static <T extends AbstractRule> void setRuleRegexProperty(AbstractRuleEntity<T> rule) {
        T r = rule.getRule();
        r.setRegex(isRegex(r.getResource()));
    }


    public static boolean isRegex(String s){
        return s.contains(REGEX_STRING);
    }

}
