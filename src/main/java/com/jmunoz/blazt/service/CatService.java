package com.jmunoz.blazt.service;

import com.jmunoz.blazt.configuration.ConfigProperties;
import com.jmunoz.blazt.exception.CatSurpriseException;
import com.jmunoz.blazt.model.CatFactResponse;
import com.jmunoz.blazt.model.CatPic;
import com.jmunoz.blazt.model.CatSurprise;
import kong.unirest.core.GenericType;
import kong.unirest.core.Unirest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CatService {

    private final ConfigProperties configProperties;

    public CatSurprise randomCatFact() throws InterruptedException {
        addRandomDelay(); // adding random delay since this one is usually faster
        var uri = String.format("%s/facts?limit=10", configProperties.getCatFactsApiBaseUrl());

        var catFacts = Unirest.get(uri)
                .asObject(CatFactResponse.class)
                .getBody()
                .data();

        return randomizeSurprise(catFacts);
    }

    public CatSurprise randomCatPic() {
        var uri = String.format("%s/v1/images/search?limit=1", configProperties.getTheCatApiBaseUrl());

        var catPics = Unirest.get(uri)
                .header(configProperties.getTheCatApiKeyHeader(), configProperties.getTheCatApiKey())
                .asObject(new GenericType<List<CatPic>>() {
                })
                .getBody();

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
