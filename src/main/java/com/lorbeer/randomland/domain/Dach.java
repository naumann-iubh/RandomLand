package com.lorbeer.randomland.domain;

public enum Dach {

    FLACHDACH("Flachdach"),
    PULTDACH("Pultdach"),
    SATTELDACH("Satteldach"),
    SATTELDACH_ERWEITERT("Satteldach Erweitert"),
    ZELTDACH("Zeltdach");

    private final String name;

    Dach(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
