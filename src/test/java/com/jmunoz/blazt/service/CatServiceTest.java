package com.jmunoz.blazt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jmunoz.blazt.configuration.ConfigProperties;
import com.jmunoz.blazt.model.CatPic;
import com.jmunoz.blazt.model.CatSurprise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.util.stream.Stream;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked", "rawtypes"})
@SpringBootTest
@ActiveProfiles("test")
public class CatServiceTest {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    private CatService catService;

    @Autowired
    private ConfigProperties configProperties;

    @SpyBean
    private ObjectMapper objectMapper;

    @MockBean
    private HttpClient httpClient;

    @MockBean
    private HttpResponse<String> mockHttpResponse;

    private static final String SAMPLE_RESPONSE_BODY = "[{\"url\": \"http://example.com/cat.jpg\"}]";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockHttpResponse.body()).thenReturn(SAMPLE_RESPONSE_BODY);

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

    @ParameterizedTest
    @MethodSource("provideHttpErrorCases")
    public void testRandomCatFact_HttpErrors(Throwable throwable) throws Exception {
        // Arrange
        when(httpClient.send(any(), eq(ofString()))).thenThrow(throwable);

        // Act & Assert
        assertThrows(throwable.getClass(), catService::randomCatFact);

        // Capturing the HttpRequest made to the httpClient and asserting it
        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(1)).send(captor.capture(), eq(ofString()));
        verifyNoInteractions(objectMapper);
    }

    @ParameterizedTest
    @MethodSource("provideHttpErrorCases")
    public void testRandomCatPic_HttpErrors(Throwable throwable) throws Exception {
        // Arrange
        when(httpClient.send(any(), eq(ofString()))).thenThrow(throwable);

        // Act & Assert
        assertThrows(throwable.getClass(), catService::randomCatPic);

        // Capturing the HttpRequest made to the httpClient and asserting it
        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(1)).send(captor.capture(), eq(ofString()));
        verifyNoInteractions(objectMapper);
        HttpRequest actualRequest = captor.getValue();

        assertEquals("cat-pic-api-key", actualRequest.headers().firstValue(CatService.THE_CAT_API_KEY_HEADER).orElse(null));
    }

    @Test
    public void testRandomCatFact_JacksonErrors() throws Exception {
        // Arrange
        String responseBody = "[{\"url\": \"http://example.com/cat.jpg\"}]";
        when(mockHttpResponse.body()).thenReturn(responseBody);
        when(httpClient.send(any(), eq(ofString()))).thenReturn(mockHttpResponse);
        doThrow(JsonProcessingException.class).when(objectMapper).readValue(eq(responseBody), any(TypeReference.class));

        // Act & Assert
        assertThrows(Exception.class, catService::randomCatFact);

        // Capturing the HttpRequest made to the httpClient and asserting it
        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(1)).send(captor.capture(), eq(ofString()));
        verify(objectMapper, times(1)).readValue(eq(responseBody), any(TypeReference.class));
    }

    @Test
    public void testRandomCatPic_JacksonErrors() throws Exception {
        // Arrange
        String responseBody = "[{\"url\": \"http://example.com/cat.jpg\"}]";
        when(mockHttpResponse.body()).thenReturn(responseBody);
        when(httpClient.send(any(), eq(ofString()))).thenReturn(mockHttpResponse);
        doThrow(JsonProcessingException.class).when(objectMapper).readValue(eq(responseBody), any(TypeReference.class));

        // Act & Assert
        assertThrows(Exception.class, catService::randomCatPic);

        // Capturing the HttpRequest made to the httpClient and asserting it
        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(1)).send(captor.capture(), eq(ofString()));
        verify(objectMapper, times(1)).readValue(eq(responseBody), any(TypeReference.class));
        HttpRequest actualRequest = captor.getValue();

        assertEquals("cat-pic-api-key", actualRequest.headers().firstValue(CatService.THE_CAT_API_KEY_HEADER).orElse(null));
    }

    private static Stream<Arguments> provideHttpErrorCases() {
        return Stream.of(
                Arguments.of(new IOException("Simulated IOException")),
                Arguments.of(new InterruptedException("Simulated InterruptedException"))
        );
    }
}
