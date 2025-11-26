// panel-baseLayers.js
/* globals map, baseLayers, addTitle */

function renderBaseLayersPanel(container) {
    addTitle(container, "Couches de carte");

    const layersContainer = document.createElement('div');
    layersContainer.style.display = 'flex';
    layersContainer.style.flexWrap = 'wrap';
    layersContainer.style.gap = '8px';
    container.appendChild(layersContainer);

    function render() {
        layersContainer.innerHTML = '';

        baseLayers.forEach(bl => {
            const layerDiv = document.createElement('div');
            layerDiv.style.position = 'relative';
            layerDiv.style.width = '120px';
            layerDiv.style.height = '90px';
            layerDiv.style.cursor = 'pointer';
            layerDiv.style.border = map.hasLayer(bl.layer)
                ? '3px solid blue'
                : '1px solid #ccc';

            const labelDiv = document.createElement('div');
            labelDiv.textContent = bl.label;
            Object.assign(labelDiv.style, {
                position: 'absolute',
                top: '0', left: '0',
                background: 'rgba(128,128,128,0.7)',
                color: 'white',
                padding: '2px 4px',
                fontSize: '12px'
            });
            layerDiv.appendChild(labelDiv);

            const canvas = document.createElement('canvas');
            canvas.width = 120;
            canvas.height = 90;
            layerDiv.appendChild(canvas);

            layerDiv.addEventListener('click', () => {
                baseLayers.forEach(b => map.removeLayer(b.layer));
                map.addLayer(bl.layer);
                render();
            });

            layersContainer.appendChild(layerDiv);

            bl._updateThumbnail = function updateThumbnail() {
                const zoom = map.getZoom();
                const center = map.getCenter();
                const scale = Math.pow(2, zoom);

                const tileX = Math.floor((center.lng + 180) / 360 * scale);
                const tileY = Math.floor(
                    (1 - Math.log(Math.tan(center.lat * Math.PI/180) + 1 / Math.cos(center.lat * Math.PI/180)) / Math.PI) / 2 * scale
                );

                const tileUrl = bl.layer.getTileUrl?.({ x: tileX, y: tileY, z: zoom });
                if (!tileUrl) return;

                const ctx = canvas.getContext('2d');
                const img = new Image();
                img.crossOrigin = 'anonymous';
                img.onload = () => {
                    ctx.clearRect(0, 0, canvas.width, canvas.height);
                    ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
                };
                img.src = tileUrl;
            };

            bl._updateThumbnail();
        });
    }

    render();
    map.on('moveend zoomend', () => baseLayers.forEach(bl => bl._updateThumbnail()));
}
