package org.pingouinfini.geojson;

import java.util.List;

public class Line {
    List<Coordonnee> coordinateArray;
    private String color;
    private String name;
    private LineStyle lineStyle;
    private Double weight;
    private ArrowStyle arrowStyle;

    public Line build() {
        return new Line(coordinateArray, name, color, lineStyle, weight, arrowStyle);
    }

    public Line(List<Coordonnee> coordinateArray) {
        this.coordinateArray = coordinateArray;
    }

    public Line(List<Coordonnee> coordinateArray, String color) {
        this.coordinateArray = coordinateArray;
        this.color = color;
    }

    public Line(List<Coordonnee> coordinateArray, String color, String name, LineStyle lineStyle, Double weight, ArrowStyle arrowStyle) {
        this.coordinateArray = coordinateArray;
        this.color = color;
        this.name = name;
        this.lineStyle = lineStyle;
        this.weight = weight;
        this.arrowStyle = arrowStyle;
    }

    public Line color(String color) {
        this.color = color;
        return this;
    }

    public Line name(String name) {
        this.name = name;
        return this;
    }

    public Line lineStyle(LineStyle lineStyle) {
        this.lineStyle = lineStyle;
        return this;
    }

    public Line weight(Double weight) {
        this.weight = weight;
        return this;
    }

    public Line arrowStyle(ArrowStyle arrowStyle) {
        this.arrowStyle = arrowStyle;
        return this;
    }

    public List<Coordonnee> getCoordinateArray() {
        return coordinateArray;
    }

    public String getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public LineStyle getLineStyle() {
        return lineStyle;
    }

    public Double getWeight() {
        return weight;
    }

    public ArrowStyle getArrowStyle() {
        return arrowStyle;
    }
}
