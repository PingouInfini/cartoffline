// panel-layers.js
/* globals objetsMetierTree, dessinsTree, kmlTree, unknownedTree,
           map, normalizeText, hasChildren, addTitle, addSeparator */

function renderLayersPanel(container) {
    addTitle(container, "Gestion des calques");

    const searchDiv = document.createElement('div');
    searchDiv.style.padding = '5px';

    const input = document.createElement('input');
    input.type = 'text';
    input.placeholder = '🔍 Filtrer les calques...';
    input.style.width = '100%';
    input.style.marginBottom = '5px';

    searchDiv.appendChild(input);
    container.appendChild(searchDiv);

    addSeparator(container);

    const treesContainer = document.createElement('div');
    container.appendChild(treesContainer);

    const allTrees = [objetsMetierTree, dessinsTree, kmlTree, unknownedTree].filter(hasChildren);
    const treeCollapseState = {};

    function render(filter = '') {
        treesContainer.innerHTML = '';

        const words = normalizeText(filter).split(/\s+/).filter(w => w);

        allTrees.forEach(tree => {
            const visibleChildren = tree.children.filter(child => {
                const text = child.label || child.name || '';
                return words.every(w => normalizeText(text).includes(w));
            });

            if (!visibleChildren.length) return;

            const groupDiv = document.createElement('div');
            groupDiv.style.marginBottom = '16px';

            const header = document.createElement('div');
            header.style.cursor = 'pointer';
            header.style.display = 'flex';
            header.style.alignItems = 'center';

            const arrow = document.createElement('span');
            arrow.textContent = treeCollapseState[tree.label] ? '▸' : '▾';
            arrow.style.marginRight = '6px';

            const groupLabel = document.createElement('strong');
            groupLabel.textContent = tree.label;

            header.appendChild(arrow);
            header.appendChild(groupLabel);
            groupDiv.appendChild(header);

            const inner = document.createElement('div');
            inner.style.marginLeft = '14px';
            inner.style.display = treeCollapseState[tree.label] ? 'none' : 'block';

            header.addEventListener('click', () => {
                treeCollapseState[tree.label] = !treeCollapseState[tree.label];
                inner.style.display = treeCollapseState[tree.label] ? 'none' : 'block';
                arrow.textContent = treeCollapseState[tree.label] ? '▸' : '▾';
            });

            // checkbox "tout sélectionner"
            if (visibleChildren.length) {
                const label = document.createElement('label');
                label.style.display = 'block';
                label.style.fontStyle = 'italic';

                const cb = document.createElement('input');
                cb.type = 'checkbox';
                cb.checked = visibleChildren.every(c => c.layer && map.hasLayer(c.layer));

                cb.addEventListener('change', () => {
                    visibleChildren.forEach(child => {
                        if (!child.layer) return;
                        if (cb.checked) map.addLayer(child.layer);
                        else map.removeLayer(child.layer);
                    });
                    render(filter);
                });

                label.appendChild(cb);
                label.appendChild(document.createTextNode(' Tout sélectionner'));
                inner.appendChild(label);
            }

            visibleChildren.forEach(child => {
                const wrap = document.createElement('div');
                wrap.style.display = 'flex';
                wrap.style.alignItems = 'center';
                wrap.style.gap = '4px';

                const cb = document.createElement('input');
                cb.type = 'checkbox';
                cb.checked = child.layer && map.hasLayer(child.layer);

                cb.addEventListener('change', () => {
                    if (!child.layer) return;
                    if (cb.checked) map.addLayer(child.layer);
                    else map.removeLayer(child.layer);
                });

                const span = document.createElement('span');
                span.textContent = child.label || child.name || '';
                span.style.cursor = 'pointer';

                span.addEventListener('click', () => {
                    const L = window.L;
                    let bounds = null;
                    let popupTarget = null;

                    const layer = child.layer;

                    if (layer instanceof L.LayerGroup) {
                        const layers = layer.getLayers();
                        const marker = layers.find(l => l instanceof L.Marker);
                        if (marker && layers.length === 2) {
                            map.setView(marker.getLatLng(), map.getZoom(), { animate: true });
                            popupTarget = marker;
                        } else {
                            bounds = layer.getBounds();
                            popupTarget = marker || layer;
                        }
                    }
                    else if (layer instanceof L.Marker) {
                        map.setView(layer.getLatLng(), map.getZoom(), { animate: true });
                        popupTarget = layer;
                    }
                    else if (layer.getBounds) {
                        bounds = layer.getBounds();
                        popupTarget = layer;
                    }

                    if (bounds) map.fitBounds(bounds, { animate: true });
                    if (popupTarget?.openPopup) setTimeout(() => popupTarget.openPopup(), 250);
                });

                wrap.appendChild(cb);
                wrap.appendChild(span);
                inner.appendChild(wrap);
            });

            groupDiv.appendChild(inner);
            treesContainer.appendChild(groupDiv);
        });
    }

    input.addEventListener('input', () => render(input.value));
    render();
}
