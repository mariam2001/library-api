package com.library.library_api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// A tiny demo controller just to show how custom values from application.properties
// get pulled into Java code. Nothing library-specific here - it's a learning example.
@RestController
@RequestMapping("/api/about")
public class AboutController {

    // @Value reads a property at startup and injects its value into this field.
    // The ${...} syntax is a "property placeholder": Spring looks up the key
    // "coach.name" in application.properties and substitutes whatever it finds.
    // If the key is missing, the app fails to start - which is usually what you want,
    // so you find out immediately rather than getting a silent null.
    @Value("${coach.name}")
    private String coachName;

    @Value("${team.name}")
    private String teamName;

    // Hitting GET /api/about returns both values as JSON, e.g.:
    // { "coach": "Mariam Ali", "team": "The cool team" }
    @GetMapping
    public Map<String, String> about() {
        return Map.of(
                "coach: ", coachName,
                "team: ", teamName
        );
    }
}
