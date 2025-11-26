window.addEventListener("DOMContentLoaded", () => {
    const slider = document.getElementById('globalOpacity');
    const valueSpan = document.getElementById('opacityValue');
    const darkOverlay = document.getElementById('darkOverlay');
    const resetBtn = document.getElementById('resetOpacity');
    const decreaseBtn = document.getElementById('decreaseOpacity');
    const increaseBtn = document.getElementById('increaseOpacity');

    if (!slider) return;

    const updateOpacity = (value) => {
        value = Math.min(2, Math.max(0, value)); // clamp 0..2
        slider.value = value;
        valueSpan.textContent = Math.round(value * 100) + '%';

        const tilePane = document.querySelector('.leaflet-tile-pane');
        if (!tilePane) return; // stop si pas encore créé

        if (value <= 1) {
            tilePane.style.opacity = value;
            if (darkOverlay) darkOverlay.style.opacity = 0;
        } else {
            const darkness = 2 - value;
            tilePane.style.opacity = darkness;
            if (darkOverlay) darkOverlay.style.opacity = 1;
        }
    };

    // Slider input
    slider.addEventListener('input', (e) => {
        updateOpacity(parseFloat(e.target.value));
    });

    // Reset
    resetBtn.addEventListener('click', () => updateOpacity(1));

    // +/- buttons
    decreaseBtn.addEventListener('click', () => updateOpacity(parseFloat(slider.value) - 0.01));
    increaseBtn.addEventListener('click', () => updateOpacity(parseFloat(slider.value) + 0.01));

    // initial update
    updateOpacity(parseFloat(slider.value));
});
