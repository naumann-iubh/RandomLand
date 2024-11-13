package com.lorbeer.randomland.domain;

public enum Baujahrklassen {

    VORKRIEGSBAU("Vorkriegsbau"),
    NACHKRIEGSBAU("Nachkriegsbau"),
    ERSTE_WAERMESCHUTZVERORDNUNG("Erste Wärmeschutzverordnung"),
    ZWEITE_WAERMESCHUTZVERORDNUNG("Zweite Wärmeschutzverordnung"),
    DRITTE_WAERMESCHUTZVERORDNUNG("Dritte Wärmeschutzverordnung"),
    ERSTE_EINSPARVERORDNUNG("Erste Einsparverordnung"),
    ZWEITE_EINSPARVERORDNUNG("Zweite Einsparverordnung"),
    ;

    private final String name;

    Baujahrklassen(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
