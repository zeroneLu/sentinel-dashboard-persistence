package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * sentinel nacos datasource properties
 */
@Data
@ToString
@NoArgsConstructor
public class SentinelNacosSourceProperties {


    private boolean mseEnabled = Boolean.FALSE;

    private List<AppSource> source;


    @Data
    @ToString
    @NoArgsConstructor
    public static class AppSource {

        private String name;

        private String instanceId;

        private String namespace;

        private String accessKey;

        private String secretKey;

        private String username;

        private String password;


    }

}
