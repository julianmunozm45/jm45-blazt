package com.jmunoz.blazt.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import kong.unirest.core.Unirest;
import kong.unirest.jackson.JacksonObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class UnirestInitializer {

    @PostConstruct
    public void init() {
        ObjectMapper jacksonMapper = new ObjectMapper();
        jacksonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Unirest.config().setObjectMapper(new JacksonObjectMapper(jacksonMapper));
    }
}
