/* globals L, getIconSize, createIcon, latLonToMgrs */

let map;
let baseLayers = {};
const mapMarkers = [];
let mapCenterItem = [];

function initializeMap() {
    const cachedLayer = L.tileLayer('cache-carto/{z}/{x}/{y}.png', {
        maxZoom: 19,
        tms: true,
        noWrap: true,
        opacity: 1,
        attribution: 'Cache local'
    });

    const osmLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        noWrap: true,
        opacity: 1,
        attribution: '© OpenStreetMap contributors'
    });

    baseLayers = [
        { label: 'Cache local', layer: cachedLayer },
        { label: 'OpenStreetMap', layer: osmLayer }
    ];

    map = L.map('map', {
        crs: L.CRS.EPSG3857,
        layers: [cachedLayer],
        attributionControl: false
    });

    map.fitBounds([
        [48.73073150423515, 2.0238402159266875],
        [48.88094147830346, 2.3018898961587952]
    ]);

    map.on('zoomend', () => {
        const zoom = map.getZoom();
        const newSize = getIconSize(zoom);
        mapMarkers.forEach(({ leafletMarker, iconUrl }) => {
            leafletMarker.setIcon(createIcon(iconUrl, newSize));
        });
    });

    map.on("popupopen", (e) => {
        const popupEl = e.popup.getElement();
        if (!popupEl) return;

        popupEl.querySelectorAll(".coord-value").forEach(span => {
            const lat = parseFloat(span.dataset.lat);  // précision complète
            const lon = parseFloat(span.dataset.lon);

            // Affichage avec 5 décimales ou MGRS selon mode
            span.textContent = formatCoordsForDisplay(lat, lon);
        });
    });
}
