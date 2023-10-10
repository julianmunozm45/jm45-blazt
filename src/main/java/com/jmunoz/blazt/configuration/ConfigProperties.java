package com.jmunoz.blazt.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class ConfigProperties {

    @Value("${THE_CAT_API_KEY}")
    private String theCatApiKey;

    private String catFactsApiBaseUrl;
    private String theCatApiBaseUrl;
    private int randomDelayMillis;

}
