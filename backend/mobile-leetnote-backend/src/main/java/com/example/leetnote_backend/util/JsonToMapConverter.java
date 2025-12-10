package com.example.leetnote_backend.util;

import com.example.leetnote_backend.exception.BadRequestException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

@Converter
public class JsonToMapConverter implements AttributeConverter<Map<String, Integer>, String> {

    private final ObjectMapper objectMapper  = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Integer> map) {
        if (map == null) return null;
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new BadRequestException("Failed to convert Map to JSON string.", e);
        }
    }

    @Override
    public Map<String, Integer> convertToEntityAttribute(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new BadRequestException("Failed to convert JSON string to Map.", e);
        }
    }
}
