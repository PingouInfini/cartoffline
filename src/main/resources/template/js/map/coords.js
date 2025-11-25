/* globals mgrs */

let coordsMode = "latlon";

// Conversion LatLon → MGRS
function latLonToMgrs(lat, lon) {
    return mgrs.forward([lon, lat], 14);
}

// Affichage dans les popups ou éléments
function formatCoordsForDisplay(lat, lon) {
    if (coordsMode === "latlon") {
        return `${lat.toFixed(5)}, ${lon.toFixed(5)}`; // 5 décimales pour affichage
    } else {
        return latLonToMgrs(lat, lon);
    }
}

// Changement du mode d'affichage
function switchCoordsFormat() {
    coordsMode = (coordsMode === "latlon") ? "mgrs" : "latlon";

    // Mettre à jour toutes les coordonnées affichées
    document.querySelectorAll(".coord-value").forEach(span => {
        const lat = parseFloat(span.dataset.lat);  // données internes complètes
        const lon = parseFloat(span.dataset.lon);
        span.textContent = formatCoordsForDisplay(lat, lon);
    });
}

// Copier les coordonnées complètes (14 décimales)
function copyDisplayedCoords(button) {
    const span = button.parentElement.querySelector(".coord-value");
    if (!span) return;

    const lat = parseFloat(span.dataset.lat);
    const lon = parseFloat(span.dataset.lon);

    let text;
    if (coordsMode === "latlon") {
        // Copie avec précision complète
        text = `${lat}, ${lon}`;
    } else {
        text = latLonToMgrs(lat, lon);
    }

    navigator.clipboard.writeText(text)
        .then(() => showToast("Coordonnées copiées : " + text + " 📋"))
        .catch(err => showToast("Erreur lors de la copie ❌"));
}

// Affichage de toast
function showToast(message, duration = 2000) {
    const toast = document.createElement('div');
    toast.textContent = message;

    Object.assign(toast.style, {
        position: 'fixed',
        top: '20px',
        left: '50%',
        transform: 'translateX(-50%)',
        background: 'rgba(0,0,0,0.8)',
        color: '#fff',
        padding: '8px 12px',
        borderRadius: '4px',
        fontSize: '14px',
        zIndex: 9999,
        opacity: 0,
        transition: 'opacity 0.3s'
    });

    document.body.appendChild(toast);
    requestAnimationFrame(() => toast.style.opacity = 1);

    setTimeout(() => {
        toast.style.opacity = 0;
        toast.addEventListener('transitionend', () => toast.remove());
    }, duration);
}
