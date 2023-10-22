package com.jmunoz.blazt.model;

public record CatFact(String fact) implements CatSurprise {
    @Override
    public String display() {
        return fact;
    }

    @Override
    public String type() {
        return "fact";
    }
}
