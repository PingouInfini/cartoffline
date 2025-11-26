let currentOpacityValue = 1;  // Variable pour stocker la valeur d'opacité

function renderOpacityPanel(container) {
    // Ajouter le titre du panneau
    addTitle(container, "Opacité de la carte");

    // Créer le contenu du slider d'opacité si ce n'est pas déjà fait
    const sliderWrapper = document.createElement('div');
    sliderWrapper.classList.add('global-slider');

    // Ajouter la structure du slider
    sliderWrapper.innerHTML = `
        <div class="slider-control-row">
            <button id="decreaseOpacity" class="slider-step-btn">-</button>
            <input type="range" id="globalOpacity" min="0" max="2" step="0.01" value="${currentOpacityValue}">
            <button id="increaseOpacity" class="slider-step-btn">+</button>
        </div>
        <div style="margin-top:8px;">
            <span id="opacityValue">${Math.round(currentOpacityValue * 100)}%</span>
            <button id="resetOpacity" class="reset-opacity-btn">Réinitialiser</button>
        </div>
    `;

    // Ajouter le slider dans le container
    container.appendChild(sliderWrapper);

    // Initialiser les éléments
    const slider = document.getElementById('globalOpacity');
    const valueSpan = document.getElementById('opacityValue');
    const darkOverlay = document.getElementById('darkOverlay');
    const resetBtn = document.getElementById('resetOpacity');
    const decreaseBtn = document.getElementById('decreaseOpacity');
    const increaseBtn = document.getElementById('increaseOpacity');

    // Fonction de mise à jour de l'opacité
    const updateOpacity = (value) => {
        value = Math.min(2, Math.max(0, value)); // Clamping entre 0 et 2
        slider.value = value;
        valueSpan.textContent = Math.round(value * 100) + '%';
        currentOpacityValue = value;  // Conserver la valeur

        const tilePane = document.querySelector('.leaflet-tile-pane');
        if (!tilePane) return;

        if (value <= 1) {
            tilePane.style.opacity = value;
            if (darkOverlay) darkOverlay.style.opacity = 0;
        } else {
            const darkness = 2 - value;
            tilePane.style.opacity = darkness;
            if (darkOverlay) darkOverlay.style.opacity = 1;
        }
    };

    // Mettre à jour l'opacité quand l'utilisateur change la valeur du slider
    slider.addEventListener('input', (e) => {
        updateOpacity(parseFloat(e.target.value));
    });

    // Réinitialiser l'opacité avec le bouton
    resetBtn.addEventListener('click', () => updateOpacity(1));

    // Boutons + et -
    decreaseBtn.addEventListener('click', () => updateOpacity(parseFloat(slider.value) - 0.01));
    increaseBtn.addEventListener('click', () => updateOpacity(parseFloat(slider.value) + 0.01));

    // Mettre à jour l'opacité au chargement initial
    updateOpacity(currentOpacityValue);

    // Afficher le slider
    sliderWrapper.style.display = 'block';
}

function hideOpacityPanel() {
    // Masquer le panneau d'opacité lorsqu'on ferme le panneau
    const sliderWrapper = document.querySelector('.global-slider');
    if (sliderWrapper) {
        sliderWrapper.style.display = 'none';
    }
}
