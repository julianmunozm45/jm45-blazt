package com.jmunoz.blazt.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jmunoz.blazt.serializer.CatSurpriseSerializer;

@JsonSerialize(using = CatSurpriseSerializer.class)
public sealed interface CatSurprise permits CatFact, CatPic {
    String display();

    String type();
}
