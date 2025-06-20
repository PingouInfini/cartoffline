package org.pingouinfini.geojson;

import java.util.List;

public class Polygon {
    List<Coordonnee> coordinateArray;
    private String name;
    private String description;
    private String color;
    private Double weight;
    private String fillColor;
    private Double fillOpacity;
    private String icon;
    private String filename;

    public Polygon build() {
        return new Polygon(coordinateArray, name, description, color, weight, fillColor, fillOpacity, icon, filename);
    }

    public Polygon(List<Coordonnee> coordinateArray) {
        this.coordinateArray = coordinateArray;
    }

    public Polygon(List<Coordonnee> coordinateArray, String name, String description) {
        this.coordinateArray = coordinateArray;
        this.name = name;
        this.description = description;
    }

    public Polygon(List<Coordonnee> coordinateArray, String name, String description, String color, Double weight, String fillColor, Double fillOpacity, String icon, String filename) {
        this.coordinateArray = coordinateArray;
        this.name = name;
        this.description = description;
        this.color = color;
        this.weight = weight;
        this.fillColor = fillColor;
        this.fillOpacity = fillOpacity;
        this.icon = icon;
        this.filename = filename;
    }

    public Polygon name(String name) {
        this.name = name;
        return this;
    }

    public Polygon description(String description) {
        this.description = description;
        return this;
    }

    public Polygon color(String color) {
        this.color = color;
        return this;
    }

    public Polygon weight(Double weight) {
        this.weight = weight;
        return this;
    }

    public Polygon fillColor(String fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    public Polygon fillOpacity(Double fillOpacity) {
        this.fillOpacity = fillOpacity;
        return this;
    }

    public Polygon icon(String icon) {
        this.icon = icon;
        return this;
    }

    public Polygon filename(String filename) {
        this.filename = filename;
        return this;
    }

    public List<Coordonnee> getCoordinateArray() {
        return coordinateArray;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getColor() {
        return color;
    }

    public Double getWeight() {
        return weight;
    }

    public String getFillColor() {
        return fillColor;
    }

    public Double getFillOpacity() {
        return fillOpacity;
    }

    public String getIcon() {
        return icon;
    }

    public String getFilename() {
        return filename;
    }
}
