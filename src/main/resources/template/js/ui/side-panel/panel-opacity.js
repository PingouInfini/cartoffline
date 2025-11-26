// panel-opacity.js
function renderOpacityPanel(container) {
    addTitle(container, "Opacité de la carte");

    const sliderWrapper = document.querySelector('.global-opacity-slider');
    if (sliderWrapper) {
        sliderWrapper.style.display = 'block';
        container.appendChild(sliderWrapper);
    }
}
