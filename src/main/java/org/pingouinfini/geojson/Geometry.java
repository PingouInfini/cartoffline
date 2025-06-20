package org.pingouinfini.geojson;

import java.util.List;

public class Geometry {
    private final String type;
    private final List<?> coordinates;

    public Geometry(String type, List<?> coordinates) {
        this.type = type;
        this.coordinates = coordinates;
    }

    public String getType() {
        return type;
    }

    public List<?> getCoordinates() {
        return coordinates;
    }
}