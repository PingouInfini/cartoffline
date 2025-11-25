let objetsMetierTree = { label: "Objets métier", selectAllCheckbox: true, collapsed: true, children: [] };
let dessinsTree      = { label: "Dessins",       selectAllCheckbox: true, collapsed: true, children: [] };
let kmlTree          = { label: "KML",           selectAllCheckbox: true, collapsed: true, children: [] };
let unknownedTree    = { label: "Inconnu",        selectAllCheckbox: true, collapsed: true, children: [] };

function hasChildren(tree) {
    return Array.isArray(tree.children) && tree.children.length > 0;
}
