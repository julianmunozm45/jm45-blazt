package com.jmunoz.blazt.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jmunoz.blazt.configuration.ConfigProperties;
import com.jmunoz.blazt.exception.CatSurpriseException;
import com.jmunoz.blazt.model.CatFact;
import com.jmunoz.blazt.model.CatPic;
import com.jmunoz.blazt.model.CatSurprise;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Random;

import static java.net.http.HttpResponse.BodyHandlers.ofString;

@Service
@RequiredArgsConstructor
public class CatService {

    public static final String THE_CAT_API_KEY_HEADER = "x-api-key";
    private final ConfigProperties configProperties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CatSurprise randomCatFact() throws InterruptedException, IOException {
        addRandomDelay(); // adding random delay since this one is usually faster

        var request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/facts", configProperties.getCatFactsApiBaseUrl())))
                .GET()
                .build();

        var response = httpClient.send(request, ofString());
        var k = response.body();
        var catFacts = objectMapper.readValue(response.body(), new TypeReference<List<CatFact>>() {
        });

        return randomizeSurprise(catFacts);
    }

    public CatSurprise randomCatPic() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/v1/images/search?limit=2", configProperties.getTheCatApiBaseUrl())))
                .header(THE_CAT_API_KEY_HEADER, configProperties.getTheCatApiKey())
                .GET()
                .build();

        var response = httpClient.send(request, ofString());
        var catPics = objectMapper.readValue(response.body(), new TypeReference<List<CatPic>>() {
        });

        return randomizeSurprise(catPics);
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
