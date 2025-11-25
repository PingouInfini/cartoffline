function createIcon(iconUrl, size = 36) {
    return L.icon({
        iconUrl: iconUrl,
        iconSize: [size, size]
    });
}

function getIconSize(zoom) {
    const baseSize = 36;
    const growthRate = 1.20;
    const cappedSize = 124;
    const size = baseSize * Math.pow(growthRate, zoom - 12);
    return Math.min(cappedSize, Math.round(size));
}
