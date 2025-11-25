const panel = document.getElementById("side-panel");
const buttons = document.getElementById("right-buttons");
const content = document.getElementById("panel-content");

let currentPanel = null;

function togglePanel(type) {
    if (panel.classList.contains("open") && currentPanel === type) {
        closePanel();
        return;
    }

    currentPanel = type;

    content.innerHTML = ''; // Clear previous content

    if (type === "baseLayers") {
        const title = document.createElement('h2');
        title.textContent = "Couches de carte";
        content.appendChild(title);

        baseLayers.forEach(bl => {
            const label = document.createElement('label');
            label.style.display = "block";
            const radio = document.createElement('input');
            radio.type = 'radio';
            radio.name = 'baseLayer';
            radio.checked = map.hasLayer(bl.layer);
            radio.addEventListener('change', () => {
                Object.values(baseLayers).forEach(b => map.removeLayer(b.layer));
                map.addLayer(bl.layer);
            });
            label.appendChild(radio);
            label.appendChild(document.createTextNode(' ' + bl.label));
            content.appendChild(label);
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

        // Trees container
        const treesContainer = document.createElement('div');
        content.appendChild(treesContainer);

        const allTrees = [objetsMetierTree, dessinsTree, kmlTree, unknownedTree].filter(hasChildren);

        function renderTrees(filter = '') {
            treesContainer.innerHTML = '';
            const words = normalizeText(filter).split(/\s+/).filter(w => w);

            allTrees.forEach(tree => {
                const groupDiv = document.createElement('div');
                groupDiv.style.marginBottom = '16px'; // <-- espacement entre catégories

                // Label de la catégorie
                const groupLabel = document.createElement('strong');
                groupLabel.textContent = tree.label;
                groupDiv.appendChild(groupLabel);

                // Checkbox "Tout sélectionner"
                if (hasChildren(tree)) {
                    const selectAllLabel = document.createElement('label');
                    selectAllLabel.style.display = 'block';
                    selectAllLabel.style.fontStyle = 'italic';
                    const selectAllCheckbox = document.createElement('input');
                    selectAllCheckbox.type = 'checkbox';
                    selectAllCheckbox.checked = tree.children.every(c => c.layer && map.hasLayer(c.layer));

                    selectAllCheckbox.addEventListener('change', () => {
                        tree.children.forEach(child => {
                            if (!child.layer) return;
                            if (selectAllCheckbox.checked) map.addLayer(child.layer);
                            else map.removeLayer(child.layer);
                        });
                        renderTrees(filter); // mettre à jour l'affichage
                    });

                    selectAllLabel.appendChild(selectAllCheckbox);
                    selectAllLabel.appendChild(document.createTextNode(' Tout sélectionner'));
                    groupDiv.appendChild(selectAllLabel);
                }

                // Objets de la catégorie
                tree.children.forEach(child => {
                    const text = child.label || child.name || '';
                    const normalized = normalizeText(text);
                    const match = words.every(w => normalized.includes(w));
                    if (!match) return;

                    const itemDiv = document.createElement('div');
                    itemDiv.style.display = 'flex';
                    itemDiv.style.alignItems = 'center';
                    itemDiv.style.gap = '4px'; // petit espace entre checkbox et texte

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
                        let popupTarget = null; // marker sur lequel ouvrir la popup

                        // Si c'est un LayerGroup
                        if (child.layer instanceof L.LayerGroup) {
                            const layers = child.layer.getLayers();

                            // Cas simple : un marker (ou le premier marker du group)
                            const marker = layers.find(l => l instanceof L.Marker);
                            if (layers.length === 2 && marker) { // ton cas ponctuel avec ligne
                                map.setView(marker.getLatLng(), map.getZoom(), { animate: true });
                                popupTarget = marker;
                            } else {
                                bounds = child.layer.getBounds();
                                // choisir un marker pour ouvrir popup si possible
                                popupTarget = marker || child.layer;
                            }
                        }
                        // Marker individuel
                        else if (child.layer instanceof L.Marker || child.layer instanceof L.Circle || child.layer instanceof L.CircleMarker) {
                            map.setView(child.layer.getLatLng(), map.getZoom(), { animate: true });
                            popupTarget = child.layer instanceof L.Marker ? child.layer : null;
                        }
                        // Polygon ou Polyline
                        else if (child.layer instanceof L.Polygon || child.layer instanceof L.Polyline) {
                            bounds = child.layer.getBounds();
                            popupTarget = child.layer;
                        }

                        // Si bounds défini, faire fitBounds
                        if (bounds) {
                            map.fitBounds(bounds, { animate: true });
                        }

                        // Ouvrir la popup si possible
                        if (popupTarget && popupTarget.openPopup) {
                            // Timeout léger pour laisser l'animation se terminer
                            setTimeout(() => popupTarget.openPopup(), 250);
                        }
                    });


                    itemDiv.appendChild(checkbox);
                    itemDiv.appendChild(textSpan);
                    groupDiv.appendChild(itemDiv);
                });

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
}
