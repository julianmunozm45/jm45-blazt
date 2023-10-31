package com.jmunoz.blazt.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked, rawtypes"})
@ExtendWith(MockitoExtension.class)
public class RestClientTest {

    @Mock
    private HttpClient httpClient;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private RestClient restClient;

    @Test
    public void testGet() throws Exception {
        // Arrange
        String uri = "http://example.com";
        Map<String, String> headers = Map.of("Header1", "Value1");
        HttpResponse mockedHttpResponse = mock(HttpResponse.class);

        String body = """
                ["mock1","mock2","mock3"]""";
        when(mockedHttpResponse.body()).thenReturn(body);
        when(httpClient.send(any(HttpRequest.class), any())).thenReturn(mockedHttpResponse);

        // Act
        Response result = restClient.get(uri, headers);

        // Assert
        assertEquals(List.of("mock1","mock2","mock3"), result.toList(String.class));
        verify(httpClient, times(1)).send(any(HttpRequest.class), any());
        verify(objectMapper, times(1)).readValue(eq(body), any(CollectionType.class));
    }
}
