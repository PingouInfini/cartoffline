// panel-renderer.js
function addTitle(container, text) {
    const h = document.createElement('h2');
    h.textContent = text;
    container.appendChild(h);
}

function addSeparator(container) {
    const sep = document.createElement('div');
    sep.style.height = '1px';
    sep.style.background = '#ccc';
    sep.style.margin = '6px 0 10px 0';
    container.appendChild(sep);
}
