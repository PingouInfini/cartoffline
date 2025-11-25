/* globals map */

let mapCenterItem = [];

window.addEventListener("DOMContentLoaded", () => {
    const slider = document.getElementById('globalOpacity');
    const valueSpan = document.getElementById('opacityValue');
    const darkOverlay = document.getElementById('darkOverlay');

    if (!slider) return;

    slider.addEventListener('input', function () {
        const value = parseFloat(this.value);
        valueSpan.textContent = Math.round(value * 100) + '%';

        if (value <= 1) {
            document.querySelector('.leaflet-tile-pane').style.opacity = value;
            if (darkOverlay) darkOverlay.style.opacity = 0;
        } else {
            const darkness = 2 - value;
            document.querySelector('.leaflet-tile-pane').style.opacity = darkness;
            if (darkOverlay) darkOverlay.style.opacity = 1;
        }
    });
});
