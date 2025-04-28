package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.client.mse.MseConfigService;
import com.alibaba.csp.sentinel.dashboard.client.mse.MsePropertyKeyConst;
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

    private final Map<String, ConfigService> configServices;

    private final Map<String, ConfigService> mseConfigServices;


    public NacosConfigClientProvider(NacosProperties nacosProperties) {

        this.nacosProperties = nacosProperties;
        this.configServices = new ConcurrentHashMap<>();
        this.mseConfigServices = new ConcurrentHashMap<>();
        this.configService = createNacosConfigService(nacosProperties.getNamespace());

    }

    public ConfigService getConfigService(String app) {
        return configServices.get(app);
    }

    public ConfigService getAdapterConfigService(String app) {
        if (properties.isMseEnabled()) {
            return mseConfigServices.get(app);
        }
        return configServices.get(app);
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
            destroyConfigServiceClient(configServices.keySet());
            return;
        }

        if (!CollectionUtils.isEmpty(configServices)) {
            Set<String> keys = namespaces.stream().map(SentinelNacosSourceProperties.AppSource::getName).collect(Collectors.toSet());
            Set<String> excludeApp = configServices.keySet().stream().filter(k -> !keys.contains(k)).collect(Collectors.toSet());
            destroyConfigServiceClient(excludeApp);
        }
        if (!CollectionUtils.isEmpty(mseConfigServices)) {
            Set<String> keys = namespaces.stream().map(SentinelNacosSourceProperties.AppSource::getName).collect(Collectors.toSet());
            Set<String> excludeApp = mseConfigServices.keySet().stream().filter(k -> !keys.contains(k)).collect(Collectors.toSet());
            destroyMseConfigServiceClient(excludeApp);
        }
        // 刷新客户端
        namespaces.forEach((n) -> {
            if (configServices.containsKey(n.getName())) {
                return;
            }
            ConfigService client = createNacosConfigService(n);
            if (client == null) {
                return;
            }
            log.info("create nacos client:{}", n.getName());
            configServices.put(n.getName(), client);

            // 创建mse 客户端
            if (!properties.isMseEnabled()) {
                return;
            }
            if (mseConfigServices.containsKey(n.getName())) {
                return;
            }
            ConfigService mseClient = createMseNacosConfigService(n);
            if (mseClient == null) {
                return;
            }
            log.info("create mse client:{}", n.getName());
            mseConfigServices.put(n.getName(), mseClient);
        });
    }

    private void destroyMseConfigServiceClient(Set<String> excludeApp) {
        if (CollectionUtils.isEmpty(excludeApp)) {
            return;
        }

        for (String app : excludeApp) {
            mseConfigServices.remove(app);
        }
    }


    private void destroyConfigServiceClient(Set<String> excludeApp) {

        if (CollectionUtils.isEmpty(excludeApp)) {
            return;
        }

        for (String app : excludeApp) {
            ConfigService destroyApp = configServices.remove(app);
            try {
                destroyApp.shutDown();
            } catch (NacosException e) {
                log.error("shutdown [{}] nacos client:error", app, e);
            }
        }

    }

    private ConfigService createNacosConfigService(String namespace) {
        return createNacosConfigService(namespace, nacosProperties.getSecretKey(), nacosProperties.getAccessKey(), nacosProperties.getUsername(), nacosProperties.getPassword(), true);
    }

    private ConfigService createNacosConfigService(SentinelNacosSourceProperties.AppSource appSource) {
        return createNacosConfigService(appSource.getNamespace(), appSource.getSecretKey(), appSource.getAccessKey(), appSource.getUsername(), appSource.getPassword(), false);
    }


    private ConfigService createNacosConfigService(String namespace, String secretKey, String accessKey, String username, String password, boolean throwError) {

        Properties properties = new Properties();
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(nacosProperties::getServerAddr).to(properties.in(PropertyKeyConst.SERVER_ADDR));
        map.from(namespace).to(properties.in(PropertyKeyConst.NAMESPACE));
        map.from(secretKey).to(properties.in(PropertyKeyConst.SECRET_KEY));
        map.from(accessKey).to(properties.in(PropertyKeyConst.ACCESS_KEY));
        map.from(username).to(properties.in(PropertyKeyConst.USERNAME));
        map.from(password).to(properties.in(PropertyKeyConst.PASSWORD));
        try {
            return ConfigFactory.createConfigService(properties);
        } catch (NacosException e) {
            if (throwError) {
                throw new IllegalStateException(e);
            }
            log.error("create nacos client error", e);
            return null;
        }
    }


    private ConfigService createMseNacosConfigService(SentinelNacosSourceProperties.AppSource appSource) {

        Properties properties = new Properties();
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(appSource.getNamespace()).to(properties.in(PropertyKeyConst.NAMESPACE));
        map.from(appSource.getAccessKey()).to(properties.in(PropertyKeyConst.ACCESS_KEY));
        map.from(appSource.getSecretKey()).to(properties.in(PropertyKeyConst.SECRET_KEY));
        map.from(appSource.getInstanceId()).to(properties.in(MsePropertyKeyConst.INSTANCE_ID));
        try {
            return MseConfigService.of(properties);
        } catch (NacosException e) {
            log.error("create mse nacos client error", e);
            return null;
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
        destroyConfigServiceClient(configServices.keySet());
    }


}
