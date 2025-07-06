var map;
var baseLayers = {};
var overlayLayers = {};
var geojsonLayer;
var cachedLayer, osmLayer;
var layerControl;
var validatedLayer = null;
var userLayers = {};

function initialize() {
    initializeMap();
    loadData();
    loadUserLayersFromStorage();
}

function initializeMap() {
    cachedLayer = L.tileLayer('cache-carto/{z}/{x}/{y}.png', {
        maxZoom: 19,
        tms: true,
        noWrap: true,
        attribution: 'Cache local'
    });

    osmLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        noWrap: true,
        attribution: '© OpenStreetMap contributors'
    });

    baseLayers = {
        "Cache local": cachedLayer,
        "OpenStreetMap": osmLayer
    };

    if (map) {
        map.off();
        map.remove();
    }

    map = L.map('map', {
        crs: L.CRS.EPSG3857,
        layers: [cachedLayer]
    }).setView([48.80558772813145, 2.1176943425396346], 12);

    if (layerControl) map.removeControl(layerControl);

    loadUserLayersFromStorage();

    layerControl = L.control.layers(baseLayers, overlayLayers, { collapsed: true }).addTo(map);
    observeLayerControlExpand();
}

function loadData() {
    const script = document.createElement('script');
    script.src = 'data/data.js';
    script.onerror = () => {
        console.error("Erreur lors du chargement de data.js");
    };
    document.body.appendChild(script);
    updateLayerControl();
}

// FORMULAIRE
function openAddLayerForm() {
    document.getElementById('add-layer-form').style.display = 'block';
    document.getElementById('validateLayerBtn').disabled = true;
    validatedLayer = null;
}

function cancelAddLayer() {
    document.getElementById('add-layer-form').style.display = 'none';
    document.getElementById('layerName').value = '';
    document.getElementById('layerURL').value = '';
    validatedLayer = null;
}

function testLayer() {
    const type = document.getElementById('layerType').value;
    const url = document.getElementById('layerURL').value;

    let testLayer;
    let tileTested = false;

    if (type === 'tile') {
        testLayer = L.tileLayer(url, { maxZoom: 19 });
    } else if (type === 'wms') {
        testLayer = L.tileLayer.wms(url, {
            layers: '',
            format: 'image/png',
            transparent: true
        });
    }

    if (!tileTested) {
        validatedLayer = testLayer;
        document.getElementById('validateLayerBtn').disabled = false;
        tileTested = true;
        setTimeout(() => map.removeLayer(testLayer), 500);
    }

    testLayer.addTo(map);
}

function validateAndAddLayer() {
    if (!validatedLayer) return;
    const name = document.getElementById('layerName').value.trim();
    if (!name) return;

    baseLayers[name] = validatedLayer;
    map.addLayer(validatedLayer);
    userLayers[name] = {
        url: document.getElementById('layerURL').value,
        type: document.getElementById('layerType').value
    };
    persistUserLayers();
    cancelAddLayer();
    updateLayerControl();
}

function updateLayerControl() {
    document.querySelectorAll(".leaflet-control-layers").forEach(el => el.remove());
    layerControl = L.control.layers(baseLayers, overlayLayers, { collapsed: true }).addTo(map);
    addCustomButtonsToBaseLayers();
    observeLayerControlExpand();
}

function addCustomButtonsToBaseLayers() {
    const items = document.querySelectorAll(".leaflet-control-layers-base input");
    items.forEach(input => {
        const label = input.parentElement;
        const layerName = label.textContent.trim();
        if (layerName !== "Cache local" && userLayers[layerName]) {
            if (!label.querySelector('.remove-btn')) {
                const btn = document.createElement("span");
                btn.innerHTML = ' ❌';
                btn.className = "remove-btn";
                btn.style.cursor = 'pointer';
                btn.onclick = function () {
                    map.removeLayer(baseLayers[layerName]);
                    delete baseLayers[layerName];
                    delete userLayers[layerName];
                    persistUserLayers();
                    updateLayerControl();
                };
                label.appendChild(btn);
            }
        }
    });

    // Ajouter le bouton ➕ add en bas
    const list = document.querySelector(".leaflet-control-layers-base");
    const addBtn = document.createElement("div");
    addBtn.innerHTML = '<span style="cursor:pointer;">➕ add</span>';
    addBtn.onclick = openAddLayerForm;
    list.appendChild(addBtn);
}

function persistUserLayers() {
    localStorage.setItem("userLayers", JSON.stringify(userLayers));
}

function loadUserLayersFromStorage() {
    const data = localStorage.getItem("userLayers");
    if (!data) return;
    try {
        userLayers = JSON.parse(data);
        for (const [name, def] of Object.entries(userLayers)) {
            let layer;
            if (def.type === "tile") {
                layer = L.tileLayer(def.url, { maxZoom: 19 });
            } else {
                layer = L.tileLayer.wms(def.url, {
                    layers: '',
                    format: 'image/png',
                    transparent: true
                });
            }
            baseLayers[name] = layer;
        }
        updateLayerControl();
    } catch (e) {
        console.error("Erreur parsing localStorage userLayers", e);
    }
}

// Détecte quand le contrôle est expand ou collapsed
function observeLayerControlExpand() {
    const control = document.querySelector(".leaflet-control-layers");
    if (!control) return;
    const observer = new MutationObserver(() => {
        const isExpanded = control.classList.contains("leaflet-control-layers-expanded");
        const addBtn = control.querySelector("span[style*='➕ add']");
        if (addBtn) {
            addBtn.style.display = isExpanded ? "inline" : "none";
        }
    });
    observer.observe(control, { attributes: true, attributeFilter: ['class'] });
}
