package com.prueba.tecnica.parqueaderos.general.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class JSONUtils {
    private static final Logger logger = LogManager.getLogger(JSONUtils.class);

    public static String mapToJSON(Map<String, String> map) {
        try {
            return new ObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            logger.error("Error convirtiendo un map a JSON {}", e.getMessage());
            return null;
        }
    }

    public static String objectToJson(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Error convirtiendo un map a JSON {}", e.getMessage());
            return null;
        }
    }

    public static <T> T jsonToObject(String json, Class<T> valueType) {
        try {
            return new ObjectMapper().readValue(json, valueType);
        } catch (JsonProcessingException e) {
            logger.error("Error convirtiendo un JSON a objeto: {}", e.getMessage());
            return null;
        }
    }
}
