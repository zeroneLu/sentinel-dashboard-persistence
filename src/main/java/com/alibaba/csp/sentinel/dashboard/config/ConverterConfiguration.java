package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.fastjson.JSON;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
public class ConverterConfiguration {
    /**
     */
    @Bean
    public Converter<List<GatewayFlowRule>, String> gatewayFlowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<GatewayFlowRule>> gatewayFlowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, GatewayFlowRule.class);
    }


    @Bean
    public Converter<List<ApiDefinition>, String> apiDefinitionEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<ApiDefinition>> apiDefinitionEntityDecoder() {
        return s -> JSON.parseArray(s, ApiDefinition.class);
    }

    /**
     */
    @Bean
    public Converter<List<FlowRule>, String> flowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<FlowRule>> flowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, FlowRule.class);
    }


    /**
     */
    @Bean
    public Converter<List<SystemRule>, String> systemRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<SystemRule>> systemRuleEntityDecoder() {
        return s -> JSON.parseArray(s, SystemRule.class);
    }


    /**
     * 网关流控规则编码解码器
     * @return
     */
    @Bean
    public Converter<List<ParamFlowRule>, String> paramFlowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<ParamFlowRule>> paramFlowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, ParamFlowRule.class);
    }

    /**
     * @return
     */
    @Bean
    public Converter<List<DegradeRule>, String> degradeRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<DegradeRule>> degradeRuleEntityDecoder() {
        return s -> JSON.parseArray(s, DegradeRule.class);
    }


    @Bean
    public Converter<List<AuthorityRule>, String> authorityRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<AuthorityRule>> authorityRuleEntityDecoder() {
        return s -> JSON.parseArray(s, AuthorityRule.class);
    }



}