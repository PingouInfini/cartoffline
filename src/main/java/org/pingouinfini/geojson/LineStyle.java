package org.pingouinfini.geojson;

import java.util.HashMap;
import java.util.Map;

public enum LineStyle {
    CONTINUOUS("Continu"),
    DOT("Pointille"),
    DASH("Tiret"),
    MIXED("Mixte"),
    MIXED_TWO_POINT("MixteDeuxPoints"),
    DOT_LONG("PointilleLong");

    private final String label;

    LineStyle(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    // Mapping inverse: label -> LineStyle
    private static final Map<String, LineStyle> LABEL_TO_ENUM = new HashMap<>();

    static {
        for (LineStyle pattern : values()) {
            LABEL_TO_ENUM.put(pattern.label, pattern);
        }
    }

    public static LineStyle fromLabel(String label) {
        return LABEL_TO_ENUM.get(label);
    }
}
