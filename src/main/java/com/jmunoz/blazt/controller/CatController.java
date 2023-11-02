package com.jmunoz.blazt.controller;

import com.jmunoz.blazt.model.CatSurprise;
import com.jmunoz.blazt.service.CatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

@RestController
@RequestMapping("/cats")
@RequiredArgsConstructor
public class CatController {

    private final CatService catService;

    @GetMapping("/pic-or-fact")
    CatSurprise getFirstCat() throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<CatSurprise>()) {
            scope.fork(catService::randomCatFact);
            scope.fork(catService::randomCatPic);

            scope.join();

            return scope.result();
        }
    }

}
