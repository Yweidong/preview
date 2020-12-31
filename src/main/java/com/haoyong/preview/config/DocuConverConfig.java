package com.haoyong.preview.config;

import com.haoyong.preview.constant.jodcConstant;
import lombok.Data;
import org.jodconverter.DocumentConverter;
import org.jodconverter.LocalConverter;
import org.jodconverter.office.LocalOfficeManager;
import org.jodconverter.office.OfficeManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: preview
 * @description:
 * @author: ywd
 * @contact:1371690483@qq.com
 * @create: 2020-12-29 17:39
 **/
@Configuration
@Data
public class DocuConverConfig {


    @Bean(
            initMethod = "start",
            destroyMethod = "stop"
    )
    public OfficeManager officeManager(){
        String os = System.getProperty("os.name").toLowerCase();
        return LocalOfficeManager.builder()
                .officeHome(os.contains("windows")? jodcConstant.JODCONVERTER_LOCAL_WIN_OFFICEHOME:jodcConstant.JODCONVERTER_LOCAL_LINUX_OFFICEHOME)
                .portNumbers(jodcConstant.JODCONVERTER_LOCAL_PORTNUMBERS)
                .maxTasksPerProcess(jodcConstant.JODCONVERTER_LOCAL_MAXTASKSPERPROCESS)
                .build();
    }
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({OfficeManager.class})
    public DocumentConverter jodConverter(OfficeManager officeManager) {
        return LocalConverter.make(officeManager);
        //return null;
    }
}
