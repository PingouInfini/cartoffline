package org.pingouinfini.geojson;

import java.util.HashMap;
import java.util.Map;

public enum DisplayLayer {
    OBJET("objetsMetierTree"),
    DESSIN("dessinsTree"),
    KML("kmlTree"),
    AUTRE("unknownedTree");

    private final String label;

    DisplayLayer(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    // Mapping inverse: label -> DisplayLayer
    private static final Map<String, DisplayLayer> LABEL_TO_ENUM = new HashMap<>();

    static {
        for (DisplayLayer pattern : values()) {
            LABEL_TO_ENUM.put(pattern.label, pattern);
        }
    }

    public static DisplayLayer fromLabel(String label) {
        return LABEL_TO_ENUM.get(label);
    }
}