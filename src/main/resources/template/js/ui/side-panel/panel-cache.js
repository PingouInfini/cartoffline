// panel-cache.js
const cacheKey = 'mapCache';

function getCacheState() {
    return JSON.parse(localStorage.getItem(cacheKey)) || {};
}

function updateCacheState(state) {
    localStorage.setItem(cacheKey, JSON.stringify(state));
}

function saveLayerState(layerName, value) {
    const cache = getCacheState();
    cache[layerName] = value;
    updateCacheState(cache);
}

function restoreLayerState(layerName) {
    const cache = getCacheState();
    return cache[layerName] || false;
}

function loadLayerFromFile(layerType, url) {
    const cache = getCacheState();

    if (cache[layerType]) return Promise.resolve(cache[layerType]);

    return fetch(url)
        .then(res => res.text())
        .then(text => {
            cache[layerType] = text;
            updateCacheState(cache);
            return text;
        });
}
