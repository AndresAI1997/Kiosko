const form = document.getElementById('product-form');
const statusEl = document.getElementById('form-status');
const headerFilter = document.querySelector('.header-filter');
if (headerFilter) {
    headerFilter.style.display = 'none';
}

form.addEventListener('submit', async event => {
    event.preventDefault();
    statusEl.textContent = 'Guardando...';
    const formData = new FormData(form);
    const params = new URLSearchParams(formData);
    try {
        const response = await fetch('http://localhost:8080/api/productos', {
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
        statusEl.textContent = 'Producto agregado correctamente';
        statusEl.classList.remove('error');
        form.reset();
    } catch (err) {
        statusEl.textContent = err.message;
        statusEl.classList.add('error');
    }
});
