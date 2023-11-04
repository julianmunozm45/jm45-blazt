package com.jmunoz.blazt.service;

import com.jmunoz.blazt.configuration.ConfigProperties;
import com.jmunoz.blazt.exception.functional.CheckedFunction;
import com.jmunoz.blazt.model.CatFact;
import com.jmunoz.blazt.model.CatFactResponse;
import com.jmunoz.blazt.model.CatPic;
import com.jmunoz.blazt.model.CatSurprise;
import kong.unirest.core.GenericType;
import kong.unirest.core.GetRequest;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked"})
@SpringBootTest
public class CatServiceTest {

    @Autowired
    private CatService catService;

    @Autowired
    private ConfigProperties configProperties;

    @Test
    public void testRandomCatFact() throws Exception {
        try (MockedStatic<Unirest> mockedUnirest = mockStatic(Unirest.class)) {
            // Arrange
            CatFactResponse mockCatFactResponse = new CatFactResponse(List.of(new CatFact("Cats are curious.")));
            HttpResponse<CatFactResponse> mockResponse = mock(HttpResponse.class);
            GetRequest mockGetRequest = mock(GetRequest.class);

            when(mockGetRequest.asObject(eq(CatFactResponse.class))).thenReturn(mockResponse);
            when(mockResponse.getBody()).thenReturn(mockCatFactResponse);

            mockedUnirest.when(() -> Unirest.get(anyString())).thenReturn(mockGetRequest);

            // Act
            CatSurprise result = catService.randomCatFact();

            // Assert
            assertEquals("Cats are curious.", result.display());

            mockedUnirest.verify(() -> Unirest.get("https://cat-fact.com/facts?limit=10"), times(1));
        }
    }

    @Test
    public void testRandomCatPic() {
        try (MockedStatic<Unirest> mockedUnirest = mockStatic(Unirest.class)) {
            // Arrange
            List<CatPic> mockCatPicsResponse = List.of(new CatPic("http://example.com/cat.jpg"));
            HttpResponse<List<CatPic>> mockResponse = mock(HttpResponse.class);
            GetRequest mockGetRequest = mock(GetRequest.class);

            ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);

            when(mockGetRequest.header(headerCaptor.capture(), headerValueCaptor.capture())).thenReturn(mockGetRequest);
            when(mockGetRequest.asObject(any(GenericType.class))).thenReturn(mockResponse);
            when(mockResponse.getBody()).thenReturn(mockCatPicsResponse);

            mockedUnirest.when(() -> Unirest.get(anyString())).thenReturn(mockGetRequest);

            // Act
            CatSurprise result = catService.randomCatPic();

            // Assert
            assertEquals("http://example.com/cat.jpg", result.display());
            assertEquals("some-api-key-h", headerCaptor.getValue());
            assertEquals("cat-pic-api-key", headerValueCaptor.getValue());

            mockedUnirest.verify(() -> Unirest.get("https://cat-pic.com/v1/images/search?limit=1"), times(1));
        }
    }

    @ParameterizedTest
    @MethodSource("provideHttpErrorCases")
    public void testRandomCatFact_HttpErrors(
            CheckedFunction<CatService, CatSurprise, Throwable> catSurpriseFn,
            Function<GetRequest, OngoingStubbing<?>> asObjectStubbingFn,
            String catApiUrl
    ) {
        try (MockedStatic<Unirest> mockedUnirest = mockStatic(Unirest.class)) {
            // Arrange
            GetRequest mockGetRequest = mock(GetRequest.class);
            asObjectStubbingFn.apply(mockGetRequest);

            mockedUnirest.when(() -> Unirest.get(anyString())).thenReturn(mockGetRequest);

            // Act & Assert
            assertThrows(UnirestException.class, () -> catSurpriseFn.apply(catService));

            mockedUnirest.verify(() -> Unirest.get(eq(catApiUrl)), times(1));
        }
    }

    // Separated tests will be more readable. I'm just plying around and exploiting Java 8+ features here.
    private static Stream<Arguments> provideHttpErrorCases() {
        CheckedFunction<CatService, CatSurprise, Throwable> catFactFn = CatService::randomCatFact;
        CheckedFunction<CatService, CatSurprise, Throwable> catPicFn = CatService::randomCatPic;

        UnirestException unirestException = new UnirestException("A timeout occurred");
        Function<GetRequest, OngoingStubbing<?>> catFactStubbing =
                (GetRequest getRequest) -> when(getRequest.asObject(eq(CatFactResponse.class))).thenThrow(unirestException);
        Function<GetRequest, OngoingStubbing<?>> catPicStubbing = (GetRequest getRequest) -> {
            when(getRequest.header(eq("some-api-key-h"), eq("cat-pic-api-key"))).thenReturn(getRequest);
            return when(getRequest.asObject(any(GenericType.class))).thenThrow(unirestException);
        };
        return Stream.of(
                Arguments.of(catFactFn, catFactStubbing, "https://cat-fact.com/facts?limit=10"),
                Arguments.of(catPicFn, catPicStubbing, "https://cat-pic.com/v1/images/search?limit=1")
        );
    }
}
