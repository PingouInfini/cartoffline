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
const mapMarkers = [];

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

    // Gestion du slider unique
    const opacitySlider = document.getElementById('globalOpacity');
    const opacityValue = document.getElementById('opacityValue');
    const darkOverlay = document.getElementById('darkOverlay');

    opacitySlider.addEventListener('input', function () {
        const value = parseFloat(this.value);
        opacityValue.textContent = Math.round(value * 100) + '%';
        if (value <= 1) {
            document.querySelector('.leaflet-tile-pane').style.opacity = value;
            darkOverlay.style.opacity = 0;
        } else {
            const darkness = 2 - value;
            document.querySelector('.leaflet-tile-pane').style.opacity = darkness;
            darkOverlay.style.opacity = 1;
        }
    });

    objetsMetierTree = { label: "Objets métier", selectAllCheckbox: true, collapsed: true, children: [] };
    dessinsTree = { label: "Dessins", selectAllCheckbox: true, collapsed: true, children: [] };
    kmlTree = { label: "KML", selectAllCheckbox: true, collapsed: true, children: [] };
    unknownedTree = { label: "Inconnu", selectAllCheckbox: true, collapsed: true, children: [] };
}

function hasChildren(tree) {
    return Array.isArray(tree.children) && tree.children.length > 0;
}

function overrideGroupLabelClicks() {
    const treeContainer = layerControl._container;

    treeContainer.querySelectorAll('.leaflet-layerstree-header-pointer').forEach(groupHeader => {
        const selectAllCheckbox = groupHeader.querySelector('.leaflet-layerstree-sel-all-checkbox');
        if (!selectAllCheckbox) return;

        const label = groupHeader.querySelector('.leaflet-layerstree-header-label');
        if (!label) return;

        label.addEventListener('click', e => {
            e.preventDefault();
            e.stopPropagation();

            const currentlyChecked = selectAllCheckbox.checked;
            const childrenContainer = groupHeader.nextElementSibling;
            if (!childrenContainer || !childrenContainer.classList.contains('leaflet-layerstree-children')) return;

            childrenContainer.querySelectorAll('input.leaflet-control-layers-selector:not(.leaflet-layerstree-sel-all-checkbox)').forEach(childCheckbox => {
                if (childCheckbox.checked === currentlyChecked) {
                    childCheckbox.click();
                }
            });

            selectAllCheckbox.checked = !currentlyChecked;
        });
    });
}

function refreshLayerControl() {
    if (layerControl) {
        map.removeControl(layerControl);
    }

    const visibleTrees = [objetsMetierTree, dessinsTree, kmlTree, unknownedTree].filter(hasChildren);

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
    addSearchToLayerControl();
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
    const baseSize = 36;
    const growthRate = 1.20;
    const cappedSize = 124;
    const size = baseSize * Math.pow(growthRate, zoom - 12);
    return Math.min(cappedSize, Math.round(size));
}

// =============== FILTRAGE DES CALQUES =========================
function addSearchToLayerControl() {
    const container = layerControl._container;

    const searchDiv = document.createElement('div');
    searchDiv.style.padding = '5px';

    const input = document.createElement('input');
    input.type = 'text';
    input.placeholder = '🔍 Filtrer les calques...';
    input.style.width = '100%';
    input.style.boxSizing = 'border-box';

    searchDiv.appendChild(input);
    container.insertBefore(searchDiv, container.firstChild);

    input.addEventListener('input', () => {
        const query = input.value.trim().toLowerCase();

        // Étape 1 — Masquer tous les noeuds non pertinents selon la recherche
        container.querySelectorAll('.leaflet-layerstree-node').forEach(node => {
            // Ne pas toucher aux groupes (ceux avec .leaflet-layerstree-header-pointer)
            if (node.querySelector('.leaflet-layerstree-header-pointer')) return;

            // Récupérer le texte du label
            const label = node.querySelector('.leaflet-layerstree-header-name');
            if (!label) return;

            const match = label.textContent.toLowerCase().includes(query);
            node.style.display = match ? '' : 'none';
        });

        // Étape 2 — Révéler ou cacher les groupes selon si au moins un enfant est visible
        container.querySelectorAll('.leaflet-layerstree-children').forEach(group => {
            const visibleChild = Array.from(group.children).some(
                child => child.style.display !== 'none'
            );
            group.style.display = visibleChild ? '' : 'none';

            const parentHeader = group.previousElementSibling;
            if (parentHeader && parentHeader.classList.contains('leaflet-layerstree-header')) {
                parentHeader.style.display = visibleChild ? '' : 'none';
            }
        });

        // Étape 3 — Masquer les autres sections non concernées
        const base = container.querySelector('.leaflet-control-layers-base');
        if (base) base.style.display = 'none';

        const separator = container.querySelector('.leaflet-control-layers-separator');
        if (separator) separator.style.display = 'none';
    });
}