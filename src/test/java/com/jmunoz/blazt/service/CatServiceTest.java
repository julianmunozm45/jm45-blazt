package com.jmunoz.blazt.service;

import com.jmunoz.blazt.configuration.ConfigProperties;
import com.jmunoz.blazt.http.Response;
import com.jmunoz.blazt.http.RestClient;
import com.jmunoz.blazt.model.CatFact;
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
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked"})
@SpringBootTest
@ActiveProfiles("test")
public class CatServiceTest {

    @Autowired
    private CatService catService;

    @Autowired
    private ConfigProperties configProperties;

    @MockBean
    private RestClient restClient;

    @MockBean
    private Response mockHttpResponse;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void testRandomCatFact() throws Exception {

        // Arrange
        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);

        when(restClient.get(uriCaptor.capture())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.toList(CatFact.class)).thenReturn(List.of(new CatFact("Cats are curious.")));

        // Act
        CatSurprise result = catService.randomCatFact();

        // Assert
        String capturedUri = uriCaptor.getValue();
        assertEquals("https://cat-fact.com/facts", capturedUri);
        assertEquals("Cats are curious.", result.display());

        verify(restClient, times(1)).get(any());
    }

    @Test
    public void testRandomCatPic() throws Exception {

        // Arrange
        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, String>> headersCaptor = ArgumentCaptor.forClass(Map.class);

        when(restClient.get(uriCaptor.capture(), headersCaptor.capture())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.toList(CatPic.class)).thenReturn(List.of(new CatPic("http://example.com/cat.jpg")));

        // Act
        CatSurprise result = catService.randomCatPic();

        // Assert
        String capturedUri = uriCaptor.getValue();
        assertEquals("https://cat-pic.com/v1/images/search?limit=2", capturedUri);
        assertEquals("http://example.com/cat.jpg", result.display());

        verify(restClient, times(1)).get(eq("https://cat-pic.com/v1/images/search?limit=2"), any());

        Map<String, String> capturedHeaders = headersCaptor.getValue();
        assertEquals("cat-pic-api-key", capturedHeaders.values().stream().findFirst().orElse(null));
    }

    @ParameterizedTest
    @MethodSource("provideHttpErrorCases")
    public void testRandomCatFact_HttpErrors(Throwable throwable) throws Exception {
        // Arrange
        when(restClient.get(any())).thenThrow(throwable);

        // Act & Assert
        assertThrows(throwable.getClass(), catService::randomCatFact);

        // Capturing the HttpRequest made to the httpClient and asserting it
        verify(restClient, times(1)).get(any());
        verifyNoInteractions(mockHttpResponse);
    }

    @ParameterizedTest
    @MethodSource("provideHttpErrorCases")
    public void testRandomCatPic_HttpErrors(Throwable throwable) throws Exception {
        // Arrange
        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        when(restClient.get(any(), captor.capture())).thenThrow(throwable);

        // Act & Assert
        assertThrows(throwable.getClass(), catService::randomCatPic);

        // Capturing the HttpRequest made to the httpClient and asserting it
        Map<String, String> capturedHeaders = captor.getValue();
        verify(restClient, times(1)).get(any(), eq(capturedHeaders));
        verifyNoInteractions(mockHttpResponse);

        assertEquals("cat-pic-api-key", capturedHeaders.values().stream().findFirst().orElse(null));
    }

    private static Stream<Arguments> provideHttpErrorCases() {
        return Stream.of(
                Arguments.of(new IOException("Simulated IOException")),
                Arguments.of(new InterruptedException("Simulated InterruptedException"))
        );
    }
}
