package com.alibaba.csp.sentinel.dashboard.rule.nacos.gateway;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.AbstractNacosPublisher;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigClientProvider;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosProperties;
import com.alibaba.csp.sentinel.datasource.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 网关流控规则Nacos发布者
 */
@Component("gatewayFlowRuleNacosPublisher")
public class GatewayFlowRuleNacosPublisher extends AbstractNacosPublisher<GatewayFlowRule> {


    protected GatewayFlowRuleNacosPublisher(NacosConfigClientProvider nacosConfigClientProvider, Converter<List<GatewayFlowRule>, String> converter, NacosProperties nacosProperties) {
        super(nacosConfigClientProvider, converter, nacosProperties);
    }

    @Override
    protected String getDataIdPostfix() {
        return NacosConfigUtil.GW_FLOW_DATA_ID_POSTFIX;
    }
}