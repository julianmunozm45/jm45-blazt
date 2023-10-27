package com.jmunoz.blazt.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

public class Response {

    private final HttpResponse<String> response;
    private final ObjectMapper objectMapper;

    public Response(HttpResponse<String> response, ObjectMapper objectMapper) {
        this.response = response;
        this.objectMapper = objectMapper;
    }

    public <T> List<T> toList(Class<T> clazz) throws IOException {
        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
        return objectMapper.readValue(response.body(), listType);
    }
}
