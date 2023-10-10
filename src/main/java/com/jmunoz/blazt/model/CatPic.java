package com.jmunoz.blazt.model;

public record CatPic(String url) implements CatSurprise {
    @Override
    public String display() {
        return url;
    }

    @Override
    public String type() {
        return "picture";
    }
}
