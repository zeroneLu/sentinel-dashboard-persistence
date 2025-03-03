package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.api.config.ConfigService;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * 抽象公用Nacos配置持有者
 *
 * 用于从nacos中获取配置
 */
@Slf4j
public abstract class NacosSourceProvider<T> implements DynamicRuleProvider<List<T>> {

    private final NacosConfigClientProvider nacosConfigClientProvider;

		//这里会根据<T>的类型引入对应已经实现好的Converter
    private final Converter<String, List<T>> converter;

    private final NacosProperties nacosProperties;

    protected NacosSourceProvider(NacosConfigClientProvider  nacosConfigClientProvider, Converter<String, List<T>> converter, NacosProperties nacosProperties) {
        this.nacosConfigClientProvider = nacosConfigClientProvider;
        this.converter = converter;
        this.nacosProperties = nacosProperties;
    }

    @Override
    public List<T> getRules(String appName) throws Exception {
        log.info("appName: {}", appName);
        log.info("nacos properties: {}", nacosProperties);

        String dataId = appName + this.getDataIdPostfix();
        String groupId = nacosProperties.getGroup();

        ConfigService configService = nacosConfigClientProvider.getConfigService(appName);

        if (configService == null) {
            log.warn("nacos [{}]config client is null,please config ",appName);
            return Collections.emptyList();
        }

        String rules = configService.getConfig(dataId, groupId, 3000);

        log.info("get rules dataId: {},groupId: {},rules: {}",dataId,groupId,rules);

        if (StringUtil.isEmpty(rules)) {
            return Collections.emptyList();
        }
        return converter.convert(rules);
    }

    /**
     * 获取datId的文件后缀
     * @return
     */
    protected abstract String getDataIdPostfix();
}