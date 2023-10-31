package com.jmunoz.blazt.service;

import com.jmunoz.blazt.configuration.ConfigProperties;
import com.jmunoz.blazt.exception.CatSurpriseException;
import com.jmunoz.blazt.http.RestClient;
import com.jmunoz.blazt.model.CatFact;
import com.jmunoz.blazt.model.CatPic;
import com.jmunoz.blazt.model.CatSurprise;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CatService {

    public static final String THE_CAT_API_KEY_HEADER = "x-api-key";

    private final RestClient restClient;
    private final ConfigProperties configProperties;

    public CatSurprise randomCatFact() throws InterruptedException, IOException {
        addRandomDelay(); // adding random delay since this one is usually faster
        var uri = String.format("%s/facts", configProperties.getCatFactsApiBaseUrl());
        var response = restClient.get(uri);

        var catFacts = response.toList(CatFact.class);

        return randomizeSurprise(catFacts);
    }

    public CatSurprise randomCatPic() throws IOException, InterruptedException {
        var uri = String.format("%s/v1/images/search?limit=2", configProperties.getTheCatApiBaseUrl());
        var response = restClient.get(uri, Map.of(THE_CAT_API_KEY_HEADER, configProperties.getTheCatApiKey()));

        var catPics = response.toList(CatPic.class);

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
