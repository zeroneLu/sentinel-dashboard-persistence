package com.alibaba.csp.sentinel.dashboard.client.mse;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.aliyun.mse20190531.Client;
import com.aliyun.mse20190531.models.*;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.Objects;
import java.util.Properties;

/**
 *
 * 阿里云Mse 接入详细参考 ：<a href="https://www.alibabacloud.com/help/zh/mse/developer-reference/api-mse-2019-05-31-overview?spm=a2c63.p38356.help-menu-123350.d_5_0_0_0.3937727ePS2pRD">open api<a/>
 *
 * <p>
 *  调用阿里云mse需要添加对应权限，如下：
 *   <p>mse:GetNacosConfig</p>
 *   <p>mse:UpdateNacosConfig</p>
 *   <p>mse:DeleteNacosConfig</p>
 * </p>
 *
 */
@Slf4j
public class MseConfigService implements ConfigService {

    private final Properties properties;

    private final Client client;

    public MseConfigService(Properties properties) throws NacosException {
        this.properties = properties;
        this.client = createClient(properties);
    }


    /**
     * 工程代码建议使用更安全的无AK方式，凭据配置方式请参见<a href="https://help.aliyun.com/document_detail/378657.html">...</a>。
     * Endpoint 请参考 <a href="https://api.aliyun.com/product/mse">...</a>
     */
    public Client createClient(Properties properties) throws NacosException {
        try {
            com.aliyun.credentials.Client credential = new com.aliyun.credentials.Client();
            Config config = new Config()
                    .setCredential(credential);
            config.endpoint = MsePropertyKeyConst.ENDPOINT;
            config.accessKeyId = properties.getProperty(MsePropertyKeyConst.ACCESS_KEY);
            config.accessKeySecret = properties.getProperty(MsePropertyKeyConst.SECRET_KEY);
            return new Client(config);
        } catch (TeaException e){
            throw new NacosException(e.getStatusCode(), e.getMessage(), e);
        }
        catch (Exception e) {
            throw new NacosException(500, e.getMessage(), e);
        }
    }


    @Override
    public String getConfig(String dataId, String group, long timeoutMs) throws NacosException {

        try {
            GetNacosConfigRequest request = new GetNacosConfigRequest();
            request.setDataId(dataId);
            request.setGroup(group);
            request.setNamespaceId(properties.getProperty(MsePropertyKeyConst.NAMESPACE));
            request.setInstanceId(properties.getProperty(MsePropertyKeyConst.INSTANCE_ID));
            GetNacosConfigResponse response = client.getNacosConfig(request);
            if (!Objects.equals(HttpStatus.OK.value(), response.getStatusCode())) {
                throw new NacosException(response.getStatusCode(), "请求发生错误");
            }
            GetNacosConfigResponseBody body = response.getBody();
            if (!body.success) {
                throw new NacosException(400, body.getErrorCode() + ":" + body.getMessage());
            }
            return body.getConfiguration().getContent();
        } catch (TeaException e) {
            throw new NacosException(e.getStatusCode(), e.getMessage(), e);
        } catch (Exception e) {
            throw new NacosException(500, e.getMessage(), e);
        }
    }

    @Override
    public String getConfigAndSignListener(String dataId, String group, long timeoutMs, Listener listener) {
        throw new IllegalStateException("unsupported getConfigAndSignListener operation");
    }

    @Override
    public void addListener(String dataId, String group, Listener listener) {
        throw new IllegalStateException("unsupported addListener operation");
    }

    @Override
    public boolean publishConfig(String dataId, String group, String content) throws NacosException {
        return publishConfig(dataId, group, content, null);
    }

    @Override
    public boolean publishConfig(String dataId, String group, String content, String type) throws NacosException {
        try {

            UpdateNacosConfigRequest request = new UpdateNacosConfigRequest();
            request.setType(type);
            request.setGroup(group);
            request.setDataId(dataId);
            request.setContent(content);
            request.setInstanceId(properties.getProperty(MsePropertyKeyConst.INSTANCE_ID));
            request.setNamespaceId(properties.getProperty(MsePropertyKeyConst.NAMESPACE));
            UpdateNacosConfigResponse response = client.updateNacosConfig(request);
            if (!Objects.equals(HttpStatus.OK.value(), response.getStatusCode())) {
                throw new NacosException( response.getStatusCode(), "请求发生错误");
            }
            UpdateNacosConfigResponseBody body = response.getBody();
            if (!body.success) {
                log.error("{}:{}", body.getErrorCode(), body.getMessage());
            }
            return body.success;
        } catch (TeaException e) {
            throw new NacosException(e.getStatusCode(), e.getMessage(), e);
        } catch (Exception e) {
            throw new NacosException(500, e.getMessage(), e);
        }
    }

    @Override
    public boolean removeConfig(String dataId, String group) throws NacosException {
        try {

            DeleteNacosConfigRequest request = new DeleteNacosConfigRequest();
            request.setGroup(group);
            request.setDataId(dataId);
            request.setNamespaceId(properties.getProperty(MsePropertyKeyConst.NAMESPACE));
            request.setInstanceId(properties.getProperty(MsePropertyKeyConst.INSTANCE_ID));
            DeleteNacosConfigResponse response = client.deleteNacosConfig(request);
            if (!Objects.equals(HttpStatus.OK.value(), response.getStatusCode())) {
                throw new NacosException(response.getStatusCode(), "请求发生错误");
            }
            DeleteNacosConfigResponseBody body = response.getBody();
            if (!body.success) {
                log.error("{}:{}", body.getErrorCode(), body.getMessage());
            }
            return body.success;
        } catch (Exception e) {
            throw new NacosException(500, e.getMessage(), e);
        }
    }

    @Override
    public void removeListener(String dataId, String group, Listener listener) {
        throw new IllegalStateException("unsupported removeListener operation");
    }

    @Override
    public String getServerStatus() {
        throw new IllegalStateException("unsupported getServerStatus operation");
    }

    @Override
    public void shutDown() {}


    public static ConfigService of(Properties properties) throws NacosException {
        return new MseConfigService(properties);
    }
}
