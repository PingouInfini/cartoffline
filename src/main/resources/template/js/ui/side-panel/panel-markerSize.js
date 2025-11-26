// panel-markerSize.js

let currentSizeMultiplier = 1;  // Valeur initiale du multiplicateur de taille (1 = taille originale)

// Fonction pour mettre à jour la taille des icônes en fonction du slider
function updateSymbolSize(sizeMultiplier) {
    currentSizeMultiplier = sizeMultiplier;

    // Réajuster la taille des icônes selon le multiplicateur
    mapMarkers.forEach(({ leafletMarker, iconUrl }) => {
        const newSize = getIconSize(map.getZoom()) * sizeMultiplier; // Appliquer le multiplicateur à la taille d'icône
        leafletMarker.setIcon(createIcon(iconUrl, newSize));  // Mettre à jour la taille de l'icône
    });
}

// Fonction pour réinitialiser la taille des symboles
function resetSymbolSize() {
    currentSizeMultiplier = 1;
    updateSymbolSize(currentSizeMultiplier);  // Réinitialiser à la taille par défaut
}

function renderMarkerSizePanel(container) {
    addTitle(container, "Gestion de la taille des symboles");

    // Créer le contenu du slider
    const sliderWrapper = document.createElement('div');
    sliderWrapper.classList.add('global-slider');  // Réutilisation de la classe pour le style

    // Ajouter la structure du slider
    sliderWrapper.innerHTML = `
        <div class="slider-control-row">
            <button id="decreaseSize" class="slider-step-btn">-</button>
            <input type="range" id="symbolSize" min="0.1" max="3" step="0.1" value="${currentSizeMultiplier}">
            <button id="increaseSize" class="slider-step-btn">+</button>
        </div>
        <div style="margin-top:8px;">
            <span id="sizeValue">${Math.round(currentSizeMultiplier * 100)}%</span>
            <button id="resetSize" class="reset-opacity-btn">Réinitialiser</button>
        </div>
    `;

    // Ajouter le slider dans le container
    container.appendChild(sliderWrapper);

    // Initialiser les éléments
    const slider = document.getElementById('symbolSize');
    const sizeSpan = document.getElementById('sizeValue');
    const resetBtn = document.getElementById('resetSize');
    const decreaseBtn = document.getElementById('decreaseSize');
    const increaseBtn = document.getElementById('increaseSize');

    // Fonction de mise à jour de la taille
    const updateSize = (value) => {
        value = Math.min(3, Math.max(0.1, value)); // Clamping entre 0.1 et 2
        slider.value = value;
        sizeSpan.textContent = Math.round(value * 100) + '%';  // Affichage en pourcentage
        updateSymbolSize(value);  // Mettre à jour la taille des symboles
    };

    // Mettre à jour la taille quand l'utilisateur change la valeur du slider
    slider.addEventListener('input', (e) => {
        updateSize(parseFloat(e.target.value));
    });

    // Réinitialiser la taille avec le bouton
    resetBtn.addEventListener('click', () => updateSize(1));

    // Boutons + et -
    decreaseBtn.addEventListener('click', () => updateSize(parseFloat(slider.value) - 0.1));
    increaseBtn.addEventListener('click', () => updateSize(parseFloat(slider.value) + 0.1));

    // Mettre à jour la taille au chargement initial
    updateSize(currentSizeMultiplier);

    // Afficher le slider
    sliderWrapper.style.display = 'block';
}
