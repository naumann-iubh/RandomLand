package com.lorbeer.randomland.domain;

import org.locationtech.jts.geom.Geometry;

import java.util.Objects;
import java.util.Random;

public final class Gebaeude {
    private Geometry shape;
    private Double height;
    private Dach dach;
    private Baujahrklassen baujahrklasse;
    private NutzungsartGebaeude usage;
    private Double avgTemp = 18.0;
    private Double normTemp = 18.0;
    private Energietraeger energy;
    private Integer wohneinheiten;

    public Gebaeude() {
    }

    public double volume() {
        final double volumeWithoutRoof = this.shape.getArea() * (height * ((double) 2 / 3));
        return volumeWithoutRoof + calculateVolumeRoof();
    }

    private double calculateVolumeRoof() {
        final double roofHeight = height * ((double) 1 / 3);
        return switch (this.dach()) {
            case Dach.ZELTDACH -> ((double) 1 / 3) * (this.shape.getArea() * roofHeight);
            case Dach.PULTDACH -> (this.shape.getArea() * roofHeight) / 2;
            case Dach.FLACHDACH -> this.shape.getArea() * roofHeight;
            case Dach.SATTELDACH ->
                    ((this.shape.getEnvelopeInternal().minExtent() * roofHeight) / 2) * this.shape.getEnvelopeInternal().maxExtent();
            default -> 0.0;
        };
    }


    public Geometry shape() {
        return shape;
    }

    public Double height() {
        return height;
    }

    public Dach dach() {
        return dach;
    }


    public NutzungsartGebaeude usage() {
        return usage;
    }

    public Double avgTemp() {
        return avgTemp;
    }

    public Double normTemp() {
        return normTemp;
    }

    public Energietraeger energy() {
        return energy;
    }

    public Integer wohneinheiten() {
        return wohneinheiten;
    }

    public Baujahrklassen baujahrklasse() {
        return baujahrklasse;
    }

    public void setBaujahrklasse(Baujahrklassen baujahrklasse) {
        this.baujahrklasse = baujahrklasse;
    }

    public void setWohneinheiten(Integer wohneinheiten) {
        this.wohneinheiten = wohneinheiten;
    }

    public void setShape(Geometry shape) {
        this.shape = shape;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public void setDach(Dach dach) {
        this.dach = dach;
    }

    public void setUsage(NutzungsartGebaeude usage) {
        this.usage = usage;
    }

    public void setAvgTemp(Double avgTemp) {
        this.avgTemp = avgTemp;
    }

    public void setNormTemp(Double normTemp) {
        this.normTemp = normTemp;
    }

    public void setEnergy(Energietraeger energy) {
        this.energy = energy;
    }

    public int toBaujahr(Baujahrklassen baujahrklasse) {
        final Random random = new Random();
        return switch (baujahrklasse) {
            case VORKRIEGSBAU -> random.nextInt(1945 - 1920) + 1920;
            case NACHKRIEGSBAU -> random.nextInt(1976 - 1946) + 1946;
            case ERSTE_WAERMESCHUTZVERORDNUNG -> random.nextInt(1983 - 1977) + 1977;
            case ZWEITE_WAERMESCHUTZVERORDNUNG -> random.nextInt(1994 - 1984) + 1984;
            case DRITTE_WAERMESCHUTZVERORDNUNG -> random.nextInt(2001 - 1995) + 1995;
            case ERSTE_EINSPARVERORDNUNG -> random.nextInt(2007 - 2002) + 2002;
            case ZWEITE_EINSPARVERORDNUNG -> random.nextInt(2024 - 2007) + 2007;
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Gebaeude) obj;
        return Objects.equals(this.shape, that.shape) && Objects.equals(this.height, that.height) && Objects.equals(this.dach, that.dach) && Objects.equals(this.baujahrklasse, that.baujahrklasse) && Objects.equals(this.usage, that.usage) && Objects.equals(this.avgTemp, that.avgTemp) && Objects.equals(this.normTemp, that.normTemp) && Objects.equals(this.energy, that.energy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shape, height, dach, baujahrklasse, usage, avgTemp, normTemp, energy);
    }

    @Override
    public String toString() {
        return "Gebaeude[" + "shape=" + shape + ", " + "height=" + height + ", " + "dach=" + dach + ", " + "baujahrklasse=" + baujahrklasse + ", " + "usage=" + usage + ", " + "avgTemp=" + avgTemp + ", " + "normTemp=" + normTemp + ", " + "energy=" + energy + ']';
    }

    public static String[] HEADER() {
        return new String[]{"WKT", "Heigth", "Dach", "Volumen", "Baujahrklasse", "Nutzungsart", "AvgTemp", "NormTemp", "Energy"};
    }
}
