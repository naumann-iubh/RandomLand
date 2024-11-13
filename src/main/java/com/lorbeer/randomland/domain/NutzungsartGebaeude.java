package com.lorbeer.randomland.domain;

public enum NutzungsartGebaeude {

    WOHNEN("Wohnen"),
    GEWERBE("Gewerbe"),
    HALLE("Halle"),
    UNBEHEIZT("Unbeheizt");

    private final String name;

    NutzungsartGebaeude(String value) {
        this.name = value;
    }

    public String getName() {
        return name;
    }
}
