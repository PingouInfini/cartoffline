/* globals layerControl, objetsMetierTree, dessinsTree, kmlTree, unknownedTree, hasChildren, normalizeText, map, mapCenterItem */

let layerControlCollapsed = false;
const allTrees = [objetsMetierTree, dessinsTree, kmlTree, unknownedTree];

function hasChildren(tree) {
    return Array.isArray(tree.children) && tree.children.length > 0;
}

/**
 * Recherche et filtrage dans les calques
 */
function filterLayers(searchQuery) {
    const words = normalizeText(searchQuery).split(/\s+/).filter(w => w);
    allTrees.forEach(tree => {
        tree.children.forEach(child => {
            const label = child.label || child.name || '';
            const normalized = normalizeText(label);
            child.visible = words.every(w => normalized.includes(w));
        });
    });
}

/**
 * Centrer la carte sur un item spécifique
 */
function centerOnItemByName(labelText) {
    const found = mapCenterItem.find(m => m.name === labelText);
    if (!found) return;

    const item = found.leafletItemId;
    let centrer;
    if (item instanceof L.Marker || item instanceof L.Circle || item instanceof L.CircleMarker) {
        centrer = item.getLatLng();
    } else if (item instanceof L.Polygon || item instanceof L.Polyline) {
        centrer = item.getBounds().getCenter();
    } else if (item instanceof L.LayerGroup && item.getBounds) {
        centrer = item.getBounds().getCenter();
    }

    map.setView(centrer, map.getZoom(), { animate: true });
    if (item.openPopup) item.openPopup();
}
