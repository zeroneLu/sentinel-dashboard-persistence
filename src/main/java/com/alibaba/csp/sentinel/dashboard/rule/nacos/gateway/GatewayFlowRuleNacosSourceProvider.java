package com.alibaba.csp.sentinel.dashboard.rule.nacos.gateway;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigClientProvider;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosProperties;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosSourceProvider;
import com.alibaba.csp.sentinel.datasource.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 网关流控规则Nacos持有者
 */
@Slf4j
@Component
public class GatewayFlowRuleNacosSourceProvider extends NacosSourceProvider<GatewayFlowRule> {


    protected GatewayFlowRuleNacosSourceProvider(NacosConfigClientProvider nacosConfigClientProvider, Converter<String, List<GatewayFlowRule>> converter, NacosProperties nacosProperties) {
        super(nacosConfigClientProvider, converter, nacosProperties);
    }

    @Override
    protected String getDataIdPostfix() {
        return NacosConfigUtil.GW_FLOW_DATA_ID_POSTFIX;
    }
}