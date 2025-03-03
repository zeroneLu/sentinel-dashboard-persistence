package com.alibaba.csp.sentinel.dashboard.util;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.validation.DataBinder;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.*;

public enum YamlPropertyLoader {
    ;
    private static final YamlPropertySourceLoader sourceLoader = new YamlPropertySourceLoader();

    private static final ConfigurationBeanBinder configurationBeanBinder = new ConfigurationBeanBinder();


    public static <T> T loadConfigurationPropertiesAs(String yaml, Class<T> type){

        ConfigurationProperties annotation = AnnotationUtils.findAnnotation(type, ConfigurationProperties.class);
        if (annotation != null){
            return loadAs(StringUtils.isNotBlank(annotation.prefix())? annotation.prefix() : annotation.value(),yaml,type);
        }
        return loadAs("",yaml,type);
    }


    public static  <T> T loadAs(String prefix, String yaml, Class<T> type){
        return loadAs(prefix,new ByteArrayResource(yaml.getBytes(StandardCharsets.UTF_8)),type);
    }

    public static  <T> T loadAs(String prefix, InputStream yamlStream, Class<T> type){
        return loadAs(prefix,new InputStreamResource(yamlStream),type);
    }

    public static  <T> T loadAs(String prefix, File file, Class<T> type){
        return loadAs(prefix,new FileSystemResource(file),type);
    }

    public static  <T> T loadAs(String prefix, Resource resource, Class<T> type){
        try {
            // 读取yaml配置文件
            List<PropertySource<?>> propertySources = sourceLoader.load(prefix, resource);
            Map<String, Object> configurationProperties = toConfigurationProperties(prefix, propertySources);
            // 构造指定类型对象
            Constructor<T> constructor = type.getConstructor();
            T result = constructor.newInstance();
            // 执行对象绑定
            configurationBeanBinder.bind(configurationProperties,true,true,result);
            return result;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static Map<String, Object> toConfigurationProperties(String prefix, List<PropertySource<?>> propertySources) {

        Map<String, Object> configurationProperties = new LinkedHashMap<>();
        propertySources.forEach(propertySource -> {
            OriginTrackedMapPropertySource mapPropertySource = (OriginTrackedMapPropertySource) propertySource;
            Map<String,Object> source =  mapPropertySource.getSource();
            source.forEach((k,v) -> {
                String newKey = toCamelCase(prefix, k);
                configurationProperties.put(newKey,propertySource.getProperty(k));
            });
        });
        return configurationProperties;
    }

    private static String toCamelCase(String prefix, String k) {

        String newKey = k.replace(prefix, "");
        StringTokenizer tokenizer = new StringTokenizer(newKey,".");
        List<String> keyStrings = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            String key = tokenizer.nextToken();
            if (key.contains("-")){
                key = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, key);
            }
            keyStrings.add(key);
        }
        return String.join(".", keyStrings);
    }


    public static class ConfigurationBeanBinder  {

        public void bind(Map<String, Object> configurationProperties, boolean ignoreUnknownFields,
                         boolean ignoreInvalidFields, Object configurationBean) {
            DataBinder dataBinder = new DataBinder(configurationBean);
            // Set ignored*
            dataBinder.setIgnoreInvalidFields(ignoreUnknownFields);
            dataBinder.setIgnoreUnknownFields(ignoreInvalidFields);
            // Get properties under specified prefix from PropertySources
            // Convert Map to MutablePropertyValues
            MutablePropertyValues propertyValues = new MutablePropertyValues(configurationProperties);
            // Bind
            dataBinder.bind(propertyValues);
        }
    }


}
