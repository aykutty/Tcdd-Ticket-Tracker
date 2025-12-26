package com.spring.yhtwatch.Service.Impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StationLookupService {

    private Map<String, Integer> nameToId;
    private Map<Integer, String> idToName;

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            nameToId = mapper.readValue(
                    new ClassPathResource("stations.json").getInputStream(),
                    new TypeReference<Map<String, Integer>>() {}
            );

            idToName = new HashMap<>();
            for (Map.Entry<String, Integer> entry : nameToId.entrySet()) {
                idToName.put(entry.getValue(), entry.getKey());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load stations.json", e);
        }
    }

    public Integer getStationId(String name) {
        return nameToId.get(name);
    }

}

