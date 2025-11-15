const inventoryData = [
    { id: 1, name: 'Chocolate 1', category: 'Golosinas', price: 3553.15, stock: 39 },
    { id: 2, name: 'Chocolate 2', category: 'Golosinas', price: 1091.6, stock: 16 },
    { id: 7, name: 'Gaseosa Cola 1', category: 'Gaseosa', price: 1500, stock: 50 },
    { id: 9, name: 'Pasta Seca 1', category: 'Pastas', price: 900, stock: 60 },
    { id: 11, name: 'Helado 1L', category: 'Productos Congelados', price: 2500, stock: 30 },
    { id: 13, name: 'Limpieza Profunda', category: 'Limpieza', price: 2800, stock: 20 }
];

const salesData = [
    { id: 301, product: 'Chocolate 1', quantity: 2, date: '2025-11-15', total: 7106.3 },
    { id: 302, product: 'Gaseosa Cola 1', quantity: 5, date: '2025-11-14', total: 7500 },
    { id: 303, product: 'Pasta Seca 1', quantity: 4, date: '2025-11-13', total: 3600 }
];

const discountData = [
    { name: 'Promo Golosinas 10%', type: 'Categoria', condition: 'Minimo 2 productos golosinas', percentage: 10, vigencia: 'Siempre' },
    { name: 'Promo Alfajor Especial', type: 'Producto', condition: 'Alfajor 1 desde 1 unidad', percentage: 5, vigencia: 'Siempre' },
    { name: 'Promo 3x2 general', type: 'Multiplicador', condition: '3x2 cualquier producto', percentage: 33, vigencia: 'Limitado' }
];

function formatCurrency(value) {
    return new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(value);
}

function renderInventory(filter = '') {
    const body = document.getElementById('inventario-body');
    const needle = filter.toLowerCase();
    body.innerHTML = '';
    inventoryData
        .filter(item =>
            item.name.toLowerCase().includes(needle) ||
            item.category.toLowerCase().includes(needle)
        )
        .forEach(item => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${item.id}</td>
                <td>${item.name}</td>
                <td>${item.category}</td>
                <td>${formatCurrency(item.price)}</td>
                <td>${item.stock}</td>`;
            body.appendChild(row);
        });
}

function renderSales(filter = '') {
    const body = document.getElementById('ventas-body');
    const needle = filter.toLowerCase();
    body.innerHTML = '';
    salesData
        .filter(item =>
            item.product.toLowerCase().includes(needle) ||
            item.date.includes(needle)
        )
        .forEach(item => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${item.id}</td>
                <td>${item.product}</td>
                <td>${item.quantity}</td>
                <td>${item.date}</td>
                <td>${formatCurrency(item.total)}</td>`;
            body.appendChild(row);
        });
}

function renderDiscounts(filter = '') {
    const body = document.getElementById('descuentos-body');
    const needle = filter.toLowerCase();
    body.innerHTML = '';
    discountData
        .filter(item =>
            item.name.toLowerCase().includes(needle) ||
            item.type.toLowerCase().includes(needle)
        )
        .forEach(item => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${item.name}</td>
                <td>${item.type}</td>
                <td>${item.condition}</td>
                <td>${item.percentage}%</td>
                <td>${item.vigencia}</td>`;
            body.appendChild(row);
        });
}

function setupNavigation() {
    const buttons = document.querySelectorAll('.nav-button');
    const panels = document.querySelectorAll('.panel');
    const headerFilter = document.querySelector('.header-filter');

    buttons.forEach(button => {
        button.addEventListener('click', () => {
            const target = button.dataset.section;
            buttons.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');

            panels.forEach(panel => {
                panel.classList.toggle('visible', panel.id === target);
            });

             if (headerFilter) {
                 headerFilter.style.display = target === 'inventario' ? 'flex' : 'none';
             }
        });
    });

    if (headerFilter) {
        const activeBtn = document.querySelector('.nav-button.active');
        headerFilter.style.display = activeBtn && activeBtn.dataset.section === 'inventario' ? 'flex' : 'none';
    }
}

function setupFilters() {
    document.getElementById('inventario-filter').addEventListener('input', e => renderInventory(e.target.value));
    document.getElementById('ventas-filter').addEventListener('input', e => renderSales(e.target.value));
    document.getElementById('descuentos-filter').addEventListener('input', e => renderDiscounts(e.target.value));
}

function init() {
    setupNavigation();
    setupFilters();
    renderInventory();
    renderSales();
    renderDiscounts();
}

document.addEventListener('DOMContentLoaded', () => {
    const isStandalone = window.location.pathname.includes('add-product.html')
        || window.location.pathname.includes('add-sale.html')
        || window.location.pathname.includes('add-discount.html');
    const headerFilter = document.querySelector('.header-filter');
    if (headerFilter) {
        headerFilter.style.display = isStandalone ? 'none' : 'flex';
    }
    if (!isStandalone) {
        init();
    }
});
