package com.alibaba.csp.sentinel.dashboard.rule.nacos.gateway;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosPublisher;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigClientProvider;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosProperties;
import com.alibaba.csp.sentinel.datasource.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 网关api分组规则Nacos发布者
 */
@Component
public class GatewayApiNacosPublisher extends NacosPublisher<ApiDefinition> {


    protected GatewayApiNacosPublisher(NacosConfigClientProvider nacosConfigClientProvider, Converter<List<ApiDefinition>, String> converter, NacosProperties nacosProperties) {
        super(nacosConfigClientProvider, converter, nacosProperties);
    }

    @Override
    protected String getDataIdPostfix() {
        return NacosConfigUtil.FLOW_DATA_ID_POSTFIX;
    }
}
