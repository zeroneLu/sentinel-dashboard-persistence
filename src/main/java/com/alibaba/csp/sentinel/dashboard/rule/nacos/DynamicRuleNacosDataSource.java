package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DynamicRuleNacosDataSource {

    private final AppManagement appManagement;

    private final Map<Class<?>,DynamicRuleProvider<List<?>>> providers;

    private final  Map<Class<?>,DynamicRulePublisher<List<?>>> publishers;

    public DynamicRuleNacosDataSource(AppManagement appManagement,
                                      ObjectProvider<List<DynamicRuleProvider<List<?>>>> providers,
                                      ObjectProvider<List<DynamicRulePublisher<List<?>>>> publishers) {
        this.appManagement = appManagement;
        this.providers = providers.getIfAvailable(ArrayList::new).stream()
                .filter(p -> p instanceof NacosSourceProvider)
                .collect(Collectors.toMap(t -> getParameterClass(t.getClass()), Function.identity()));
        this.publishers = publishers.getIfAvailable(ArrayList::new).stream()
                .filter(p -> p instanceof NacosPublisher)
                .collect(Collectors.toMap(t -> getParameterClass(t.getClass()),Function.identity()));
    }

    public static Class<?>  getParameterClass(Class<?> clazz) {
        ResolvableType resolvableType = ResolvableType.forClass(clazz);
        return  resolvableType.getSuperType().getGeneric().resolve();
    }


    public <T,R> List<R> getRules(String appName, Class<T> rule, EntityConverter<T,R> converter) throws Exception {

        if (StringUtil.isBlank(appName)) {
            return Collections.emptyList();
        }
        List<MachineInfo> list = appManagement.getDetailApp(appName).getMachines()
                .stream()
                .filter(MachineInfo::isHealthy)
                .sorted((e1, e2) -> Long.compare(e2.getLastHeartbeat(), e1.getLastHeartbeat())).collect(Collectors.toList());
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        MachineInfo machine = list.get(0);
        DynamicRuleProvider<List<?>> provider = providers.getOrDefault(rule, DynamicRuleProvider.DEFAULT_PROVIDER);
        List<?> rules =  provider.getRules(appName);
        if (CollectionUtils.isEmpty(rules)){
            return Collections.emptyList();
        }
        List<T> castRules = rules.stream().map(rule::cast).collect(Collectors.toList());
        return castRules.stream().map(r -> converter.map(machine.getApp(),machine.getIp(),machine.getPort(),r)).collect(Collectors.toList());

    }

    public <T,R> void publish(String app, List<T> rules, Class<R> c, Function<T,R> fun) throws Exception {

        List<R> r = rules.stream().map(fun).collect(Collectors.toList());
        publishers.getOrDefault(c, DynamicRulePublisher.DEFAULT_PUBLISHER).publish(app,r);
    }


    public interface EntityConverter<T,R> {

        R map(String app, String ip, Integer port, T rule);
    }


}
