package org.pingouinfini.geojson;

import java.util.List;

public class Polygon {
    List<Coordonnee> coordinateArray;
    private String name;
    private String description;
    private String color;
    private LineStyle lineStyle;
    private Double weight;
    private FillPattern fillPattern;
    private String fillColor;
    private Double fillOpacity;
    private String icon;
    private String filename;

    public Polygon build() {
        return new Polygon(coordinateArray, name, description, color, lineStyle, weight, fillPattern, fillColor, fillOpacity, icon, filename);
    }

    public Polygon(List<Coordonnee> coordinateArray) {
        this.coordinateArray = coordinateArray;
    }

    public Polygon(List<Coordonnee> coordinateArray, String name, String description) {
        this.coordinateArray = coordinateArray;
        this.name = name;
        this.description = description;
    }

    public Polygon(List<Coordonnee> coordinateArray, String name, String description, String color, LineStyle lineStyle, Double weight, FillPattern fillPattern, String fillColor, Double fillOpacity, String icon, String filename) {
        this.coordinateArray = coordinateArray;
        this.name = name;
        this.description = description;
        this.color = color;
        this.lineStyle = lineStyle;
        this.weight = weight;
        this.fillPattern = fillPattern;
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

    public Polygon lineStyle(LineStyle lineStyle) {
        this.lineStyle = lineStyle;
        return this;
    }

    public Polygon borderColor(String color) {
        this.color = color;
        return this;
    }

    public Polygon weight(Double weight) {
        this.weight = weight;
        return this;
    }

    public Polygon fillPattern(FillPattern fillPattern) {
        this.fillPattern = fillPattern;
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

    public LineStyle getLineStyle() {
        return lineStyle;
    }

    public Double getWeight() {
        return weight;
    }

    public FillPattern getFillPattern() {
        return fillPattern;
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

