package com.lorbeer.randomland.domain;

public enum Energietraeger {

    HEIZOEL("Heizöl"),
    ERDGAS("Erdgas"),
    FERNWAERME("Fernwärme");

    private String name;

    Energietraeger(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
