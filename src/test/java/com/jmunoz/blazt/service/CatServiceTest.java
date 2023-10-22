package com.jmunoz.blazt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jmunoz.blazt.configuration.ConfigProperties;
import com.jmunoz.blazt.model.CatPic;
import com.jmunoz.blazt.model.CatSurprise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked", "rawtypes"})
@SpringBootTest
@ActiveProfiles("test")
public class CatServiceTest {

    @Autowired
    private CatService catService;

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HttpClient httpClient;

    @MockBean
    private HttpResponse<String> mockHttpResponse;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void testRandomCatFact() throws Exception {

        // Arrange
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        ArgumentCaptor<BodyHandler> bodyHandlerCaptor = ArgumentCaptor.forClass(BodyHandler.class);

        when(httpClient.send(requestCaptor.capture(), bodyHandlerCaptor.capture())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.body()).thenReturn("[{\"fact\": \"Cats are curious.\"}]");

        // Act
        CatSurprise result = catService.randomCatFact();

        // Assert
        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("https://cat-fact.com/facts", capturedRequest.uri().toString());
        assertEquals("Cats are curious.", result.display());

        verify(httpClient, times(1)).send(any(), eq(ofString()));
    }

    @Test
    public void testRandomCatPic() throws Exception {
        // Arrange
        String responseBody = "[{\"url\": \"http://example.com/cat.jpg\"}]";  // Modify this to resemble actual response
        when(mockHttpResponse.body()).thenReturn(responseBody);
        when(httpClient.send(any(), eq(ofString()))).thenReturn(mockHttpResponse);

        // Act
        CatSurprise result = catService.randomCatPic();

        // Assert
        assertNotNull(result);
        assertEquals(new CatPic("http://example.com/cat.jpg"), result);  // Adapt this as per your actual expectation

        // Capturing the HttpRequest made to the httpClient and asserting it
        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(1)).send(captor.capture(), eq(ofString()));
        HttpRequest actualRequest = captor.getValue();

        assertEquals("cat-pic-api-key", actualRequest.headers().firstValue(CatService.THE_CAT_API_KEY_HEADER).orElse(null));
    }
}
