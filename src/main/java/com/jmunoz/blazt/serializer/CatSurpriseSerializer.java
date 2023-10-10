package com.jmunoz.blazt.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.jmunoz.blazt.model.CatSurprise;

import java.io.IOException;

public class CatSurpriseSerializer extends StdSerializer<CatSurprise> {

    public CatSurpriseSerializer() {
        this(null);
    }

    public CatSurpriseSerializer(Class<CatSurprise> t) {
        super(t);
    }

    @Override
    public void serialize(
            CatSurprise catSurprise, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {

        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("display", catSurprise.display());
        jsonGenerator.writeStringField("type", catSurprise.type());
        jsonGenerator.writeEndObject();
    }
}

