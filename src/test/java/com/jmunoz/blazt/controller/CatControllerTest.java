package com.jmunoz.blazt.controller;

import com.jmunoz.blazt.configuration.ConfigProperties;
import com.jmunoz.blazt.model.CatFact;
import com.jmunoz.blazt.model.CatPic;
import com.jmunoz.blazt.model.CatSurprise;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
//@EnabledInNativeImage
public class CatControllerTest {

    @Autowired
    CatController catController;

    @MockBean
    RestTemplate restTemplate;

    @MockBean
    ConfigProperties configProperties;

    @Test
    @DisabledInNativeImage
    public void testCatFact() throws Exception {
        Mockito.when(configProperties.getRandomDelayMillis()).thenReturn(500);
        Mockito.when(configProperties.getCatFactsApiBaseUrl()).thenReturn("https://cat-fact.com");
        Mockito.when(restTemplate.exchange(
                        eq("https://cat-fact.com/facts"),
                        eq(HttpMethod.GET),
                        eq(null),
                        eq(new ParameterizedTypeReference<List<CatFact>>() {
                        })))
                .thenReturn(ResponseEntity.ok(List.of(new CatFact("Cats are curious."))));

        CatSurprise catSurprise = catController.getFirstCat();

        assertEquals("fact", catSurprise.type());
        assertEquals("Cats are curious.", catSurprise.display());
    }

    @Test
    @DisabledInNativeImage
    public void testCatPic() throws Exception {
        Mockito.when(configProperties.getTheCatApiBaseUrl()).thenReturn("https://cat-pic.com");
        Mockito.when(configProperties.getTheCatApiKey()).thenReturn("cat-pic-api-key");
        Mockito.when(restTemplate.exchange(
                        eq("https://cat-pic.com/v1/images/search?limit=2"),
                        eq(HttpMethod.GET),
                        any(),
                        eq(new ParameterizedTypeReference<List<CatPic>>() {
                        })))
                .thenReturn(ResponseEntity.ok(List.of(new CatPic("http://example.com/cat.jpg"))));

        CatSurprise catSurprise = catController.getFirstCat();

        assertEquals("picture", catSurprise.type());
        assertEquals("http://example.com/cat.jpg", catSurprise.display());
    }
}
