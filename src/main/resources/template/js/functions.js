/* globals L, fetch */

// =================== VARIABLES GLOBALES ======================
let map;
let baseLayers = {};
let overlayLayers = {};
let objetsMetierTree;
let dessinsTree;
let kmlTree;
let unknownedTree;
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
        opacity: 1,
        attribution: 'Cache local'
    });

    const osmLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        noWrap: true,
        opacity: 1,
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

    // Gestion du slider unique
    const globalSlider = document.getElementById('globalOpacity');
    const opacityLabel = document.getElementById('opacityValue');

    const layers = [cachedLayer, osmLayer];

    globalSlider.addEventListener('input', () => {
        const value = parseFloat(globalSlider.value);
        opacityLabel.textContent = Math.round(value * 100) + '%';
        layers.forEach(layer => layer.setOpacity(value));
    });

    objetsMetierTree = {
        label: "Objets métier",
        selectAllCheckbox: true,
        collapsed: true,
        children: []
    };

    dessinsTree = {
        label: "Dessins",
        selectAllCheckbox: true,
        collapsed: true,
        children: []
    };

    kmlTree = {
        label: "KML",
        selectAllCheckbox: true,
        collapsed: true,
        children: []
    };

    unknownedTree = {
        label: "Inconnu",
        selectAllCheckbox: true,
        collapsed: true,
        children: []
    };
}

function hasChildren(tree) {
    return Array.isArray(tree.children) && tree.children.length > 0;
}

function hasChildren(tree) {
    return Array.isArray(tree.children) && tree.children.length > 0;
}

function overrideGroupLabelClicks() {
    const treeContainer = layerControl._container;

    // Pour chaque noeud contenant un selectAllCheckbox (les groupes)
    treeContainer.querySelectorAll('.leaflet-layerstree-header-pointer').forEach(groupHeader => {
        const selectAllCheckbox = groupHeader.querySelector('.leaflet-layerstree-sel-all-checkbox');

        if (!selectAllCheckbox) return;

        // Cibler uniquement le header, sans les spans d'expansion
        const label = groupHeader.querySelector('.leaflet-layerstree-header-label');
        if (!label) return;

        // Empêche le comportement d'expansion/repli
        label.addEventListener('click', e => {
            e.preventDefault();
            e.stopPropagation();

            const currentlyChecked = selectAllCheckbox.checked;

            // Trouver tous les enfants de ce groupe
            const childrenContainer = groupHeader.nextElementSibling;
            if (!childrenContainer || !childrenContainer.classList.contains('leaflet-layerstree-children')) return;

            // Pour chaque checkbox enfant, on simule un click si son état ne correspond pas
            childrenContainer.querySelectorAll('input.leaflet-control-layers-selector:not(.leaflet-layerstree-sel-all-checkbox)').forEach(childCheckbox => {
                if (childCheckbox.checked === currentlyChecked) {
                    childCheckbox.click();
                }
            });

            // Inverser l’état de la case selectAll
            selectAllCheckbox.checked = !currentlyChecked;
        });
    });
}

function refreshLayerControl() {
    if (layerControl) {
        map.removeControl(layerControl);
    }

    const visibleTrees = [objetsMetierTree, dessinsTree, kmlTree, unknownedTree]
        .filter(hasChildren);

    layerControl = L.control.layers.tree(baseLayers, {
        label: 'Calques',
        children: visibleTrees,
    }, {
        selectorBack: false,
        collapsed: false,
        closedSymbol: '&#8862; &#x1f5c0;',
        openedSymbol: '&#8863; &#x1f5c1;',
    }).addTo(map);

    overrideGroupLabelClicks();
}

// =============== CHARGEMENT DES DONNÉES ======================
function loadData() {
    const script = document.createElement('script');
    script.src = 'data/data.js';
    script.onload = () => {
        refreshLayerControl();
    };
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