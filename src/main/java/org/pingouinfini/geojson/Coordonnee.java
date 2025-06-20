package org.pingouinfini.geojson;

public class Coordonnee {
    private final Double latitude;
    private final Double longitude;

    public Coordonnee(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return "[" + latitude + ", " + longitude + "]";
    }
}
