package com.haoyong.preview.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @program: preview
 * @description: 阿里云短信
 * @author: ywd
 * @contact:1371690483@qq.com
 * @create: 2021-01-12 17:42
 **/
@Component
@ConfigurationProperties(prefix = "aliyun.sms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AliSMSConfig {
    private String accessKeyId;
    private String accessKeySecret;
    private String product;
    private String domain;
    private String regionId;
    private String endpointName;
    private String signName;
    private String codeTemplate;
    private String version;
    private String action;

}
