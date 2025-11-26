// panel-baseLayers.js
/* globals map, baseLayers, addTitle */

function renderBaseLayersPanel(container) {
    addTitle(container, "Couches de carte");

    // Container vertical
    const layersContainer = document.createElement('div');
    layersContainer.style.display = 'flex';
    layersContainer.style.flexDirection = 'column';
    layersContainer.style.alignItems = 'center'; // centre les items horizontalement
    layersContainer.style.width = '100%'; // largeur max du panel
    container.appendChild(layersContainer);

    function render() {
        layersContainer.innerHTML = '';

        baseLayers.forEach(bl => {
            // Layer wrapper
            const layerDiv = document.createElement('div');
            layerDiv.style.position = 'relative';
            layerDiv.style.width = '120px';
            layerDiv.style.height = '90px';
            layerDiv.style.cursor = 'pointer';
            layerDiv.style.marginBottom = '8px';
            layerDiv.style.border = map.hasLayer(bl.layer)
                ? '3px solid blue'
                : '1px solid #ccc';
            layerDiv.style.borderRadius = '4px';
            layerDiv.style.overflow = 'hidden';
            layerDiv.style.display = 'flex';
            layerDiv.style.alignItems = 'center';

            // Label
            const labelDiv = document.createElement('div');
            labelDiv.textContent = bl.label;
            Object.assign(labelDiv.style, {
                position: 'absolute',
                top: '2px',
                left: '4px',
                background: 'rgba(0,0,0,0.5)',
                color: '#fff',
                padding: '2px 6px',
                fontSize: '12px',
                borderRadius: '2px'
            });
            layerDiv.appendChild(labelDiv);

            // Canvas miniature
            const canvas = document.createElement('canvas');
            canvas.width = 120;
            canvas.height = 120;
            canvas.style.marginLeft = 'auto';
            canvas.style.marginRight = 'auto';
            canvas.style.display = 'block';
            layerDiv.appendChild(canvas);

            // Clic sur layer : activation
            layerDiv.addEventListener('click', () => {
                baseLayers.forEach(b => map.removeLayer(b.layer));
                map.addLayer(bl.layer);
                render(); // rafraîchir la bordure
            });

            layersContainer.appendChild(layerDiv);

            // Fonction de mise à jour de la vignette
            bl._updateThumbnail = function updateThumbnail() {
                if (!map) return;
                const zoom = map.getZoom();
                const center = map.getCenter();

                const ctx = canvas.getContext('2d');
                const img = new Image();

                // On différencie OSM et cache local
                let tileUrl;

                if (bl.label === 'OpenStreetMap') {
                    // OpenStreetMap : pas de TMS, URL directe
                    const scale = Math.pow(2, zoom);
                    const tileX = Math.floor((center.lng + 180) / 360 * scale);
                    const tileY = Math.floor(
                        (1 - Math.log(Math.tan(center.lat * Math.PI/180) + 1 / Math.cos(center.lat * Math.PI/180)) / Math.PI) / 2 * scale
                    );
                    tileUrl = `https://a.tile.openstreetmap.org/${zoom}/${tileX}/${tileY}.png`; // tu peux changer {s} par a/b/c si tu veux
                } else {
                    // Cache local TMS
                    const scale = Math.pow(2, zoom);
                    const tileX = Math.floor((center.lng + 180) / 360 * scale);

                    const yXYZ = Math.floor(
                        (1 - Math.log(Math.tan(center.lat * Math.PI/180) + 1 / Math.cos(center.lat * Math.PI/180)) / Math.PI) / 2 * scale
                    );
                    const tileY = Math.pow(2, zoom) - 1 - yXYZ; // conversion TMS
                    tileUrl = `cache-carto/${zoom}/${tileX}/${tileY}.png`;
                }

                img.onload = () => {
                    ctx.clearRect(0, 0, canvas.width, canvas.height);
                    ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
                };
                img.onerror = () => {
                    ctx.clearRect(0, 0, canvas.width, canvas.height);
                    ctx.fillStyle = '#ccc';
                    ctx.fillRect(0, 0, canvas.width, canvas.height);
                    ctx.fillStyle = '#333';
                    ctx.font = '10px sans-serif';
                    ctx.fillText('Tuile manquante', 20, 65);
                };

                img.src = tileUrl;
            };

            // Mise à jour initiale
            bl._updateThumbnail();
        });
    }

    render();

    // Mettre à jour les miniatures lors du zoom / déplacement
    if (map) {
        map.on('moveend zoomend', () => baseLayers.forEach(bl => bl._updateThumbnail()));
    }
}
