const form = document.getElementById('sale-form');
const statusEl = document.getElementById('sale-status');
const headerFilter = document.querySelector('.header-filter');
if (headerFilter) {
    headerFilter.style.display = 'none';
}

form.addEventListener('submit', async event => {
    event.preventDefault();
    statusEl.textContent = 'Guardando...';
    const params = new URLSearchParams(new FormData(form));
    try {
        const response = await fetch('http://localhost:8080/api/ventas', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: params.toString()
        });
        if (!response.ok) {
            const error = await response.json().catch(() => ({ error: 'Error desconocido' }));
            throw new Error(error.error || 'No se pudo guardar');
        }
        statusEl.textContent = 'Venta guardada.';
        statusEl.classList.remove('error');
        form.reset();
    } catch (err) {
        statusEl.textContent = err.message;
        statusEl.classList.add('error');
    }
});
