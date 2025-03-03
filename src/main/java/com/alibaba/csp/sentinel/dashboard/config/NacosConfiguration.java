package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigClientProvider;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//只有存在NacosProperties实体的时候才启用
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(NacosProperties.class)
public class NacosConfiguration {

    @Bean
    public NacosConfigClientProvider nacosConfigClientProvider(NacosProperties nacosProperties){
        return new NacosConfigClientProvider(nacosProperties);
    }


}
