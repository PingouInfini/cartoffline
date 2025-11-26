const panel = document.getElementById("side-panel");
const buttons = document.getElementById("right-buttons");
const content = document.getElementById("panel-content");
const treeCollapseState = {};

let currentPanel = null;
const cacheKey = 'mapCache'; // Clé pour stocker l'état dans localStorage

// Fonction pour obtenir l'état du cache local
function getCacheState() {
    const cache = JSON.parse(localStorage.getItem(cacheKey)) || {};
    return cache;
}

// Fonction pour mettre à jour l'état dans le cache local
function updateCacheState(state) {
    localStorage.setItem(cacheKey, JSON.stringify(state));
}

// Fonction pour sauvegarder les calques visibles dans le cache
function saveLayerState(layerName, isVisible) {
    const cache = getCacheState();
    cache[layerName] = isVisible;
    updateCacheState(cache);
}

// Fonction pour restaurer les calques visibles à partir du cache
function restoreLayerState(layerName) {
    const cache = getCacheState();
    return cache[layerName] || false;
}

// Fonction pour charger un fichier depuis le cache ou par un accès local
function loadLayerFromFile(layerType, url) {
    const cache = getCacheState();
    if (cache[layerType]) {
        // Si le fichier est déjà en cache, l'utiliser
        return cache[layerType];
    } else {
        // Charger le fichier si non trouvé dans le cache (exemple : fichier KML)
        fetch(url)
            .then(response => response.text())
            .then(data => {
                // Sauvegarder le contenu du fichier dans le cache pour réutilisation
                cache[layerType] = data;
                updateCacheState(cache);
                return data;
            })
            .catch(error => console.error('Erreur de chargement du fichier:', error));
    }
}

function togglePanel(type) {
    if (panel.classList.contains("open") && currentPanel === type) {
        closePanel();
        return;
    }

    currentPanel = type;
    content.innerHTML = ''; // Clear previous content

    // Mettre à jour les boutons de droite
    document.querySelectorAll('#right-buttons .action-btn').forEach(btn => {
        btn.classList.remove('active-btn');
    });

    // Trouver le bouton cliqué
    const btn = document.querySelector(`#right-buttons .action-btn[onclick="togglePanel('${type}')"]`);
    if (btn) btn.classList.add('active-btn');

    if (type === "baseLayers") {
        const title = document.createElement('h2');
        title.textContent = "Couches de carte";
        content.appendChild(title);

        const layersContainer = document.createElement('div');
        layersContainer.style.display = 'flex';
        layersContainer.style.flexWrap = 'wrap';
        layersContainer.style.gap = '8px';
        content.appendChild(layersContainer);

        function renderBaseLayers() {
            layersContainer.innerHTML = '';

            baseLayers.forEach(bl => {
                const layerDiv = document.createElement('div');
                layerDiv.style.position = 'relative';
                layerDiv.style.width = '120px';
                layerDiv.style.height = '90px';
                layerDiv.style.cursor = 'pointer';
                layerDiv.style.border = map.hasLayer(bl.layer) ? '3px solid blue' : '1px solid #ccc';
                layerDiv.style.boxSizing = 'border-box';

                // Label en haut à gauche
                const labelDiv = document.createElement('div');
                labelDiv.textContent = bl.label;
                Object.assign(labelDiv.style, {
                    position: 'absolute',
                    top: '0',
                    left: '0',
                    background: 'rgba(128,128,128,0.7)',
                    color: 'white',
                    fontSize: '12px',
                    padding: '2px 4px',
                    zIndex: 10
                });
                layerDiv.appendChild(labelDiv);

                // Mini-canvas pour afficher la tuile centrale
                const canvas = document.createElement('canvas');
                canvas.width = 120;
                canvas.height = 90;
                layerDiv.appendChild(canvas);

                // Clic pour sélectionner la couche
                layerDiv.addEventListener('click', () => {
                    baseLayers.forEach(b => map.removeLayer(b.layer));
                    map.addLayer(bl.layer);
                    saveLayerState(bl.label, true); // Sauvegarder l'état de la couche active
                    renderBaseLayers();
                });

                layersContainer.appendChild(layerDiv);

                // Fonction pour mettre à jour la vignette
                function updateThumbnail() {
                    const zoom = map.getZoom();
                    const center = map.getCenter();
                    const tileSize = 256;
                    const scale = Math.pow(2, zoom);

                    const tileX = Math.floor((center.lng + 180) / 360 * scale);
                    const tileY = Math.floor(
                        (1 - Math.log(Math.tan(center.lat * Math.PI / 180) + 1 / Math.cos(center.lat * Math.PI / 180)) / Math.PI) / 2 * scale
                    );

                    const tileUrl = bl.layer.getTileUrl
                        ? bl.layer.getTileUrl({ x: tileX, y: tileY, z: zoom })
                        : '';

                    if (!tileUrl) return;

                    const ctx = canvas.getContext('2d');
                    const img = new Image();
                    img.crossOrigin = 'anonymous';
                    img.onload = () => {
                        ctx.clearRect(0, 0, canvas.width, canvas.height);
                        ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
                    };
                    img.src = tileUrl;
                }

                // Stocker pour mise à jour plus tard
                bl._updateThumbnail = updateThumbnail;
                updateThumbnail();
            });
        }

        renderBaseLayers();

        // Mettre à jour les vignettes à chaque mouvement ou zoom
        map.on('moveend zoomend', () => {
            baseLayers.forEach(bl => bl._updateThumbnail());
        });
    }

    else if (type === "layers") {
        const title = document.createElement('h2');
        title.textContent = "Gestion des calques";
        content.appendChild(title);

        // Search input
        const searchDiv = document.createElement('div');
        searchDiv.style.padding = '5px';
        const input = document.createElement('input');
        input.type = 'text';
        input.placeholder = '🔍 Filtrer les calques...';
        input.style.width = '100%';
        input.style.boxSizing = 'border-box';
        input.style.marginBottom = '5px';
        searchDiv.appendChild(input);
        content.appendChild(searchDiv);

        // --- Séparateur visuel ---
        const separator = document.createElement('div');
        separator.style.height = '1px';
        separator.style.background = '#ccc';
        separator.style.margin = '6px 0 10px 0';
        content.appendChild(separator);

        // Trees container
        const treesContainer = document.createElement('div');
        content.appendChild(treesContainer);

        const allTrees = [objetsMetierTree, dessinsTree, kmlTree, unknownedTree].filter(hasChildren);

        function renderTrees(filter = '') {
            treesContainer.innerHTML = '';
            const words = normalizeText(filter).split(/\s+/).filter(w => w);

            allTrees.forEach(tree => {
                // Filtrage des enfants visibles
                const visibleChildren = tree.children.filter(child => {
                    const text = child.label || child.name || '';
                    const normalized = normalizeText(text);
                    return words.every(w => normalized.includes(w));
                });

                // Si aucun enfant visible → ne pas afficher la catégorie
                if (visibleChildren.length === 0) return;

                // Conteneur du groupe
                const groupDiv = document.createElement('div');
                groupDiv.style.marginBottom = '16px';

                // --- Header avec collapse ---
                const header = document.createElement('div');
                header.style.display = 'flex';
                header.style.alignItems = 'center';
                header.style.cursor = 'pointer';
                header.style.userSelect = 'none';

                const arrow = document.createElement('span');
                arrow.textContent = treeCollapseState[tree.label] ? '▸' : '▾';
                arrow.style.marginRight = '6px';

                const groupLabel = document.createElement('strong');
                groupLabel.textContent = tree.label;

                header.appendChild(arrow);
                header.appendChild(groupLabel);
                groupDiv.appendChild(header);

                // Conteneur interne collapsable
                const inner = document.createElement('div');
                inner.style.marginLeft = '14px';
                inner.style.display = treeCollapseState[tree.label] ? 'none' : 'block';

                // --- Clic = collapse toggle ---
                header.addEventListener('click', () => {
                    treeCollapseState[tree.label] = !treeCollapseState[tree.label];
                    inner.style.display = treeCollapseState[tree.label] ? "none" : "block";
                    arrow.textContent = treeCollapseState[tree.label] ? '▸' : '▾';
                });

                // --- Checkbox "Tout sélectionner" — affichée seulement s'il y a des objets ---
                if (visibleChildren.length > 0) {
                    const selectAllLabel = document.createElement('label');
                    selectAllLabel.style.display = 'block';
                    selectAllLabel.style.fontStyle = 'italic';

                    const selectAllCheckbox = document.createElement('input');
                    selectAllCheckbox.type = 'checkbox';

                    // Calcul de l'état sélectionné
                    selectAllCheckbox.checked = visibleChildren.every(c => c.layer && map.hasLayer(c.layer));

                    selectAllCheckbox.addEventListener('change', () => {
                        visibleChildren.forEach(child => {
                            if (!child.layer) return;
                            if (selectAllCheckbox.checked) map.addLayer(child.layer);
                            else map.removeLayer(child.layer);
                        });
                        renderTrees(filter);
                    });

                    selectAllLabel.appendChild(selectAllCheckbox);
                    selectAllLabel.appendChild(document.createTextNode(' Tout sélectionner'));
                    inner.appendChild(selectAllLabel);
                }

                // --- Items individuels ---
                visibleChildren.forEach(child => {
                    const text = child.label || child.name || '';
                    const itemDiv = document.createElement('div');
                    itemDiv.style.display = 'flex';
                    itemDiv.style.alignItems = 'center';
                    itemDiv.style.gap = '4px';

                    const checkbox = document.createElement('input');
                    checkbox.type = 'checkbox';
                    checkbox.checked = child.layer && map.hasLayer(child.layer);
                    checkbox.addEventListener('change', () => {
                        if (!child.layer) return;
                        if (checkbox.checked) map.addLayer(child.layer);
                        else map.removeLayer(child.layer);
                    });

                    const textSpan = document.createElement('span');
                    textSpan.textContent = text;
                    textSpan.style.cursor = 'pointer';
                    textSpan.addEventListener('click', () => {
                        if (!child.layer) return;

                        let bounds = null;
                        let popupTarget = null;

                        if (child.layer instanceof L.LayerGroup) {
                            const layers = child.layer.getLayers();
                            const marker = layers.find(l => l instanceof L.Marker);
                            if (layers.length === 2 && marker) {
                                map.setView(marker.getLatLng(), map.getZoom(), { animate: true });
                                popupTarget = marker;
                            } else {
                                bounds = child.layer.getBounds();
                                popupTarget = marker || child.layer;
                            }
                        }
                        else if (child.layer instanceof L.Marker ||
                                 child.layer instanceof L.Circle ||
                                 child.layer instanceof L.CircleMarker) {
                            map.setView(child.layer.getLatLng(), map.getZoom(), { animate: true });
                            popupTarget = child.layer instanceof L.Marker ? child.layer : null;
                        }
                        else if (child.layer instanceof L.Polygon ||
                                 child.layer instanceof L.Polyline) {
                            bounds = child.layer.getBounds();
                            popupTarget = child.layer;
                        }

                        if (bounds) map.fitBounds(bounds, { animate: true });

                        if (popupTarget && popupTarget.openPopup) {
                            setTimeout(() => popupTarget.openPopup(), 250);
                        }
                    });

                    itemDiv.appendChild(checkbox);
                    itemDiv.appendChild(textSpan);
                    inner.appendChild(itemDiv);
                });

                groupDiv.appendChild(inner);
                treesContainer.appendChild(groupDiv);
            });
        }

        input.addEventListener('input', () => renderTrees(input.value));
        renderTrees();
    }

    else if (type === "opacity") {
        const title = document.createElement('h2');
        title.textContent = "Opacité de la carte";
        content.appendChild(title);

        const sliderWrapper = document.querySelector('.global-opacity-slider');
        if (sliderWrapper) {
            sliderWrapper.style.display = 'block';
            content.appendChild(sliderWrapper);
        }
    }

    else if (type === "markerSize") {
        const title = document.createElement('h2');
        title.textContent = "Gestion de la taille des symboles";
        content.appendChild(title);
        // Rien d'autre pour l'instant
    }

    else if (type === "statistics") {
            const title = document.createElement('h2');
            title.textContent = "Statistiques";
            content.appendChild(title);
            // Rien d'autre pour l'instant
        }

    else if (type === "options") {
        const title = document.createElement('h2');
        title.textContent = "Options";
        content.appendChild(title);
        // Rien d'autre pour l'instant
    }

    panel.classList.add("open");
    buttons.classList.add("shifted");
}

function closePanel() {
    panel.classList.remove("open");
    buttons.classList.remove("shifted");
    currentPanel = null;

    const sliderWrapper = document.querySelector('.global-opacity-slider');
    if (sliderWrapper) {
        sliderWrapper.style.display = 'none';
        document.body.appendChild(sliderWrapper);
    }

    document.querySelectorAll('#right-buttons .action-btn').forEach(btn => {
        btn.classList.remove('active-btn');
    });
}
