package com.jmunoz.blazt.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.net.http.HttpResponse.BodyHandlers.ofString;

@Component
@RequiredArgsConstructor
public class RestClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public Response get(String uri, Map<String, String> headers) throws IOException, InterruptedException {
        String[] headersArray = headers.entrySet().stream()
                .map(entry -> List.of(entry.getKey(), entry.getValue()))
                .flatMap(Collection::stream)
                .toArray(String[]::new);


        var request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .headers(headersArray)
                .GET()
                .build();

        var httpResponse = httpClient.send(request, ofString());
        return new Response(httpResponse, objectMapper);
    }

    public Response get(String uri) throws IOException, InterruptedException {
        return get(uri, Map.of());
    }
}
