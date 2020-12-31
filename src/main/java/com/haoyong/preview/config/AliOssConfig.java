package com.haoyong.preview.config;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.auth.DefaultCredentials;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: preview
 * @description:
 * @author: ywd
 * @contact:1371690483@qq.com
 * @create: 2020-12-28 15:34
 **/
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
@Data
public class AliOssConfig {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String urlPrefix;
    private String bucketUrl;


    /**
     *
     *OSS服务的配置类
     */
    private static ClientConfiguration clientConfiguration() {
        ClientConfiguration conf = new ClientConfiguration();
        // 设置TCP连接超时为5000毫秒
        conf.setConnectionTimeout(5000);

        // 设置最大的重试次数为3
        conf.setMaxErrorRetry(3);

        // 设置Socket传输数据超时的时间为2000毫秒
        conf.setSocketTimeout(2000);
        return conf;
    }

    @Bean
    public OSSClient ossClient() {
        return new OSSClient(endpoint,new DefaultCredentialProvider(new DefaultCredentials(accessKeyId,accessKeySecret)),clientConfiguration());
    }
}
