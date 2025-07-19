/* globals L, fetch */

// =================== VARIABLES GLOBALES ======================
let map;
let baseLayers = {};
let overlayLayers = {};
let ponctuelGroup;
let surfaciqueGroup;
let ligneGroup;
let layerControl;

// =============== INITIALISATION ==============================
function initialize() {
    initializeMap();
    loadData();
}

function initializeMap() {
    const cachedLayer = L.tileLayer('cache-carto/{z}/{x}/{y}.png', {
        maxZoom: 19,
        tms: true,
        noWrap: true,
        attribution: 'Cache local'
    });

    const osmLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        noWrap: true,
        attribution: '© OpenStreetMap contributors'
    });

    baseLayers = [{
            label: 'Cache local',
            layer: cachedLayer
        },
        {
            label: 'OpenStreetMap',
            layer: osmLayer
        }
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

        mapMarkers.forEach(({
            leafletMarker,
            iconUrl
        }) => {
            leafletMarker.setIcon(createIcon(iconUrl, newSize));
        });
    });

    ponctuelGroup = L.layerGroup().addTo(map);
    surfaciqueGroup = L.layerGroup().addTo(map);
    ligneGroup = L.layerGroup().addTo(map);
    kmlGroup = L.layerGroup().addTo(map);

    const treeOverlays = {
        label: 'Calques',
        children: [{
                label: 'Objets Métier',
                selectAllCheckbox: true,
                children: [{
                        label: 'Ponctuel',
                        layer: ponctuelGroup
                    },
                    {
                        label: 'Surfacique',
                        layer: surfaciqueGroup
                    },
                    {
                        label: 'Lignes',
                        layer: ligneGroup
                    },
                ]
            },
            {
                label: 'KML',
                selectAllCheckbox: true,
                children: [{
                    label: 'KML',
                    layer: kmlGroup
                }]

            }
        ]
    };

    layerControl = L.control.layers.tree(baseLayers, treeOverlays, {
        namedToggle: false,
        selectorBack: false,
        collapsed: true,
    }).addTo(map);
}

// =============== CHARGEMENT DES DONNÉES ======================
function loadData() {
    const script = document.createElement('script');
    script.src = 'data/data.js';
    script.onerror = () => console.error('Erreur lors du chargement de data.js');
    document.body.appendChild(script);
}

// =============== GESTION DES ICONES (Markers) ================
function createIcon(iconUrl, size = 36) {
    return L.icon({
        iconUrl: iconUrl,
        iconSize: [size, size]
    });
}

function getIconSize(zoom) {
    const baseSize = 36; // taille à zoom 12
    const growthRate = 1.20; // taux de croissance exponentielle
    const cappedSize = 124; // taille maximale

    const size = baseSize * Math.pow(growthRate, zoom - 12);
    return Math.min(cappedSize, Math.round(size));
}

const mapMarkers = [];