function normalizeText(text) {
    return text.normalize("NFD")
               .replace(/[\u0300-\u036f]/g, "")
               .toLowerCase();
}

function addSearchToLayerControl() {
    const container = layerControl._container;

    const searchDiv = document.createElement('div');
    searchDiv.style.padding = '5px';
    searchDiv.style.position = 'relative';

    const input = document.createElement('input');
    input.type = 'text';
    input.placeholder = '🔍 Filtrer les calques...';
    input.style.width = '100%';
    input.style.boxSizing = 'border-box';
    input.style.paddingRight = '24px';

    const clearBtn = document.createElement('span');
    clearBtn.textContent = '❌';
    clearBtn.style.position = 'absolute';
    clearBtn.style.right = '10px';
    clearBtn.style.top = '50%';
    clearBtn.style.transform = 'translateY(-50%)';
    clearBtn.style.cursor = 'pointer';
    clearBtn.style.display = 'none';

    input.addEventListener('input', () => {
        const query = input.value.trim();
        clearBtn.style.display = query ? 'inline' : 'none';

        const words = normalizeText(query).split(/\s+/).filter(w => w);

        container.querySelectorAll('.leaflet-layerstree-node').forEach(node => {
            if (node.querySelector('.leaflet-layerstree-header-pointer')) return;
            const label = node.querySelector('.leaflet-layerstree-header-name');
            if (!label) return;

            const normalized = normalizeText(label.textContent);
            const match = words.every(w => normalized.includes(w));
            node.style.display = match ? '' : 'none';
        });

        container.querySelectorAll('.leaflet-layerstree-children').forEach(group => {
            const visibleChild = Array.from(group.children).some(c => c.style.display !== 'none');
            group.style.display = visibleChild ? '' : 'none';

            const parent = group.previousElementSibling;
            if (parent?.classList.contains('leaflet-layerstree-header')) {
                parent.style.display = visibleChild ? '' : 'none';
            }
        });

        const base = container.querySelector('.leaflet-control-layers-base');
        if (base) base.style.display = 'none';

        const sep = container.querySelector('.leaflet-control-layers-separator');
        if (sep) sep.style.display = 'none';
    });

    clearBtn.addEventListener('click', () => {
        input.value = '';
        input.dispatchEvent(new Event('input'));
        input.focus();
    });

    searchDiv.appendChild(input);
    searchDiv.appendChild(clearBtn);
    container.insertBefore(searchDiv, container.firstChild);
}
