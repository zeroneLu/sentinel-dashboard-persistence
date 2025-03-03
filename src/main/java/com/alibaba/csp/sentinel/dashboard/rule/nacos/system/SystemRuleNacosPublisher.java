/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.rule.nacos.system;

import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosPublisher;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigClientProvider;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosProperties;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 */
@Component
public class SystemRuleNacosPublisher extends NacosPublisher<SystemRule> {


    protected SystemRuleNacosPublisher(NacosConfigClientProvider nacosConfigClientProvider, Converter<List<SystemRule>, String> converter, NacosProperties nacosProperties) {
        super(nacosConfigClientProvider, converter, nacosProperties);
    }

    @Override
    protected String getDataIdPostfix() {
        return NacosConfigUtil.SYSTEM_DATA_ID_POSTFIX;
    }
}
