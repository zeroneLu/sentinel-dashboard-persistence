package com.alibaba.csp.sentinel.dashboard.rule.nacos.gateway;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigClientProvider;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosProperties;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosSourceProvider;
import com.alibaba.csp.sentinel.datasource.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 网关api分组规则Nacos持有者；从naocs获取到配置
 */
@Slf4j
@Component
public class GatewayApiNacosSourceProvider extends NacosSourceProvider<ApiDefinition> {


    protected GatewayApiNacosSourceProvider(NacosConfigClientProvider nacosConfigClientProvider, Converter<String, List<ApiDefinition>> converter, NacosProperties nacosProperties) {
        super(nacosConfigClientProvider, converter, nacosProperties);
    }

    @Override
    protected String getDataIdPostfix() {
        return NacosConfigUtil.FLOW_DATA_ID_POSTFIX;
    }
}
