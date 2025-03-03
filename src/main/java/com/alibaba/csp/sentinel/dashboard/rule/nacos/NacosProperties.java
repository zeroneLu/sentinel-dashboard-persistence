package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "sentinel.nacos")
@ToString
public class NacosProperties {

    private String serverAddr;

    private String username;

    private String password;

    private String group = "SENTINEL_GROUP";

    private String dataId;

    private String endpoint;

    private String namespace;

    private String accessKey;

    private String secretKey;

    private int timeout = 3000;
}