package org.pingouinfini.geojson;

import java.util.HashMap;
import java.util.Map;

public enum FillPattern {
    NONE("Aucun"),
    FULL("Plein"),
    DIAGONAL_LEFT("DegreeDroite"), //inversion volontaire
    DIAGONAL_RIGHT("DegreeGauche"),
    GRID("Grille"),
    MESH("Maillage"),
    HORIZONTAL("Horizontal"),
    VERTICAL("Vertical");

    private final String label;

    FillPattern(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    // Mapping inverse: label -> FillPattern
    private static final Map<String, FillPattern> LABEL_TO_ENUM = new HashMap<>();

    static {
        for (FillPattern pattern : values()) {
            LABEL_TO_ENUM.put(pattern.label, pattern);
        }
    }

    public static FillPattern fromLabel(String label) {
        return LABEL_TO_ENUM.get(label);
    }
}