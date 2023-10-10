package com.jmunoz.blazt.model;

public record CatFact(String text) implements CatSurprise {
    @Override
    public String display() {
        return text;
    }

    @Override
    public String type() {
        return "fact";
    }
}
