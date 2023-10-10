package com.jmunoz.blazt.controller;

import com.jmunoz.blazt.configuration.ConfigProperties;
import com.jmunoz.blazt.exception.CatSurpriseException;
import com.jmunoz.blazt.model.CatFact;
import com.jmunoz.blazt.model.CatPic;
import com.jmunoz.blazt.model.CatSurprise;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

@RestController
@RequestMapping("/cats")
@RequiredArgsConstructor
public class CatController {

    public static final String THE_CAT_API_KEY_HEADER = "x-api-key";
    private final ConfigProperties configProperties;
    private final RestTemplate restTemplate;

    @GetMapping("/pic-or-fact")
    CatSurprise getFirstCat() throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<CatSurprise>()) {
            scope.fork(this::catFact);
            scope.fork(this::catPic);

            scope.join();

            return scope.result();
        }
    }

    private CatSurprise catFact() throws InterruptedException {
        addRandomDelay(); // adding random delay since this one is usually faster
        ResponseEntity<List<CatFact>> response = restTemplate.exchange(
                String.format("%s/facts", configProperties.getCatFactsApiBaseUrl()),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        var catFacts = response.getBody();

        return randomizeSurprise(catFacts);
    }

    private CatSurprise catPic() {
        var headers = new HttpHeaders();
        headers.set(THE_CAT_API_KEY_HEADER, configProperties.getTheCatApiKey());

        var entity = new HttpEntity<>(headers);
        ResponseEntity<List<CatPic>> response = restTemplate.exchange(
                String.format("%s/v1/images/search?limit=2", configProperties.getTheCatApiBaseUrl()),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        var catSearchResult = response.getBody();

        return randomizeSurprise(catSearchResult);
    }

    private CatSurprise randomizeSurprise(List<? extends CatSurprise> meowList) {
        if (meowList != null && !meowList.isEmpty()) {
            var random = new Random();
            var randomIndex = random.nextInt(meowList.size());
            return meowList.get(randomIndex);
        }
        throw new CatSurpriseException("Failed to fetch you cat surprise");
    }

    private void addRandomDelay() throws InterruptedException {
        var random = new Random();
        var randomDelayMillis = random.nextInt(configProperties.getRandomDelayMillis());
        Thread.sleep(randomDelayMillis);
    }

}
