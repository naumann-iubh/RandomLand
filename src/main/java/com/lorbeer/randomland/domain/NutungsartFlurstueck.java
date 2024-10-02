package com.lorbeer.randomland.domain;

public enum NutungsartFlurstueck {

    PARK("Park"),
    STRASSE("Straße"),
    GEBAUDE("Gebäude");

    private final String name;

    NutungsartFlurstueck(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
