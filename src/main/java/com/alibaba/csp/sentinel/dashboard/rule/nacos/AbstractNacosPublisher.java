package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.nacos.api.config.ConfigService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 抽象公用Nacos规则发布者
 *
 * 用于把配置发布到nacos保存
 */
@Slf4j
public abstract class AbstractNacosPublisher<T> implements DynamicRulePublisher<List<T>> {

    private final NacosConfigClientProvider nacosConfigClientProvider;

  	//这里会根据<T>的类型引入对应已经实现好的Converter
    private final Converter<List<T>, String> converter;


    private final NacosProperties nacosProperties;


    protected AbstractNacosPublisher(NacosConfigClientProvider nacosConfigClientProvider, Converter<List<T>, String> converter, NacosProperties nacosProperties) {
        this.nacosConfigClientProvider = nacosConfigClientProvider;
        this.converter = converter;
        this.nacosProperties = nacosProperties;
    }

    @Override
    public void publish(String app, List<T> rules) throws Exception {
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (rules == null) {
            return;
        }

        String dataId = app + this.getDataIdPostfix();
        String groupId = nacosProperties.getGroup();

        log.info("publish; dataId: {},groupId: {},rules: {}",dataId,groupId,rules);

        ConfigService configService = nacosConfigClientProvider.getConfigService(app);

        if (configService == null){
            log.warn("nacos [{} ]config client is null,please config ",app);
            return;
        }


        configService.publishConfig(dataId,groupId, converter.convert(rules));
    }

    /**
     * 获取datId的文件后缀
     * @return
     */
    protected abstract String getDataIdPostfix();
}