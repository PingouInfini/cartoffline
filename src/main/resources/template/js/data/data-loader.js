function loadData() {
    const script = document.createElement('script');
    script.src = 'data/data.js';
    script.onload = () => {};
    script.onerror = () => console.error('Erreur lors du chargement de data.js');
    document.body.appendChild(script);
}