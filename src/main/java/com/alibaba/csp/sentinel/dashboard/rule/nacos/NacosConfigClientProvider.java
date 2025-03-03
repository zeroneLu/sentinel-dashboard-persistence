package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.util.YamlPropertyLoader;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class NacosConfigClientProvider implements InitializingBean, DisposableBean {

    protected final Logger log = LoggerFactory.getLogger(getClass());


    private static final String SENTINEL_DATA_SOURCE = "sentinel-datasource-config.yaml";


    @Getter
    private SentinelNacosSourceProperties properties;


    private final ConfigService configService;


    private final NacosProperties nacosProperties;


    private final Map<String, ConfigService> configServiceMap;



    public NacosConfigClientProvider(NacosProperties nacosProperties) {

        this.nacosProperties = nacosProperties;
        this.configServiceMap = new ConcurrentHashMap<>();
        this.configService = createNacosConfigService(nacosProperties.getNamespace());

    }

    public ConfigService getConfigService(String app) {
        return configServiceMap.get(app);
    }



    private Listener createListener() {
        return new PropertyListener();
    }


    class PropertyListener implements Listener {


        @Override
        public Executor getExecutor() {
            return null;
        }

        @Override
        public void receiveConfigInfo(String configInfo) {
            try {
                refreshConfiguration(configInfo);
            } catch (Exception e) {
                log.error("refresh dataId [{}] error, {}", SENTINEL_DATA_SOURCE, configInfo, e);
            }
        }
    }


    private void initialConfiguration() {

        try {
            String configInfo =
                    configService
                            .getConfig(SENTINEL_DATA_SOURCE, nacosProperties.getGroup(), nacosProperties.getTimeout());
            refreshConfiguration(configInfo);
        } catch (Exception e) {
            log.error("add configuration dataId [{}] error", SENTINEL_DATA_SOURCE, e);
        }
    }

    private void refreshConfiguration(String configInfo) {
        if (StringUtils.isBlank(configInfo)) {
            log.warn("remove config:{}", SENTINEL_DATA_SOURCE);
            return;
        }
        this.properties = YamlPropertyLoader.loadAs("sentinel-datasource", configInfo, SentinelNacosSourceProperties.class);

        List<SentinelNacosSourceProperties.AppSource> namespaces = properties.getSource();
        if (CollectionUtils.isEmpty(namespaces)) {
            destroyConfigServiceClient(configServiceMap.keySet());
            return;
        }

        if (!CollectionUtils.isEmpty(configServiceMap)) {
            Set<String> keys = namespaces.stream().map(SentinelNacosSourceProperties.AppSource::getName).collect(Collectors.toSet());
            Set<String> excludeApp = configServiceMap.keySet().stream().filter(k -> !keys.contains(k)).collect(Collectors.toSet());
            destroyConfigServiceClient(excludeApp);
        }
        // 刷新客户端
        namespaces.forEach((n) -> configServiceMap.computeIfAbsent(n.getName(), k -> createNacosConfigService(n.getNamespace())));
    }


    private void destroyConfigServiceClient(Set<String> excludeApp) {

        if (CollectionUtils.isEmpty(excludeApp)) {
            return;
        }

        for (String app : excludeApp) {
            ConfigService destroyApp = configServiceMap.remove(app);
            try {
                destroyApp.shutDown();
            } catch (NacosException e) {
                log.error("shutdown [{}] nacos client:error", app, e);
            }
        }

    }


    private ConfigService createNacosConfigService(String namespace) {

        Properties properties = new Properties();
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(namespace).to(properties.in(PropertyKeyConst.NAMESPACE));
        map.from(nacosProperties::getServerAddr).to(properties.in(PropertyKeyConst.SERVER_ADDR));
        map.from(nacosProperties::getSecretKey).to(properties.in(PropertyKeyConst.SECRET_KEY));
        map.from(nacosProperties::getAccessKey).to(properties.in(PropertyKeyConst.ACCESS_KEY));
        map.from(nacosProperties::getUsername).to(properties.in(PropertyKeyConst.USERNAME));
        map.from(nacosProperties::getPassword).to(properties.in(PropertyKeyConst.PASSWORD));
        try {
            return ConfigFactory.createConfigService(properties);
        } catch (NacosException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class Properties extends java.util.Properties {

        <V> java.util.function.Consumer<V> in(String key) {
            return (value) -> put(key, value);
        }


    }


    @Override
    public void afterPropertiesSet() {
        //
        initialConfiguration();
        // register Listener
        try {
            configService.addListener(SENTINEL_DATA_SOURCE, nacosProperties.getGroup(), createListener());
        } catch (NacosException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void destroy() {
        destroyConfigServiceClient(configServiceMap.keySet());
    }


}
