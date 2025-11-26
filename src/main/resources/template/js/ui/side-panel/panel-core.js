// panel-core.js
/* globals renderBaseLayersPanel, renderLayersPanel, renderOpacityPanel,
          renderMarkerSizePanel, renderStatisticsPanel, renderOptionsPanel */

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
    content.innerHTML = '';

    document.querySelectorAll('#right-buttons .action-btn')
        .forEach(btn => btn.classList.remove('active-btn'));

    const btn = document.querySelector(`#right-buttons .action-btn[onclick="togglePanel('${type}')"]`);
    if (btn) btn.classList.add('active-btn');

    switch (type) {
        case "baseLayers":   renderBaseLayersPanel(content); break;
        case "layers":       renderLayersPanel(content); break;
        case "opacity":      renderOpacityPanel(content); break;
        case "markerSize":   renderMarkerSizePanel(content); break;
        case "statistics":   renderStatisticsPanel(content); break;
        case "options":      renderOptionsPanel(content); break;
    }

    panel.classList.add("open");
    buttons.classList.add("shifted");
}

function closePanel() {
    panel.classList.remove("open");
    buttons.classList.remove("shifted");
    currentPanel = null;

    // Masquer le panneau de l'opacité quand on ferme
    const sliderWrapper = document.querySelector('.global-opacity-slider');
    if (sliderWrapper) {
        sliderWrapper.style.display = 'none';  // Cache le slider
    }

    document.querySelectorAll('#right-buttons .action-btn')
        .forEach(btn => btn.classList.remove('active-btn'));
}