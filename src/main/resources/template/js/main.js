/* globals initializeMap, loadData, refreshLayerControl */

window.addEventListener("DOMContentLoaded", () => {
    initializeMap();
    loadData(); // Appelle refreshLayerControl() une fois les données prêtes
});
