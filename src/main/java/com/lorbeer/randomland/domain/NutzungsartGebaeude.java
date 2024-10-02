package com.lorbeer.randomland.domain;

public enum NutzungsartGebaeude {

    WOHNEN("Wohnen"),
    GEWERBE("Gewerbe"),
    HALLE("Halle"),
    UNBEHEIZT("Unbeheizt");

    private String value;

    NutzungsartGebaeude(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
