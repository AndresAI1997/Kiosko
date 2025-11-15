# Bolurubro Kiosko

Proyecto full-stack que simula el funcionamiento de un kiosko de barrio: manejo de inventario, ventas y descuentos. Se divide en dos carpetas independientes para facilitar el despliegue.

## 🗂️ Estructura

```
backend/
  src/com/kiosko/...        # código Java (modelos, servicios, API HTTP)
  productos_kiosko.csv      # base de datos simple en CSV
frontend/
  index.html                # panel principal (inventario, ventas, descuentos)
  add-product.html/.js      # formulario de productos
  add-sale.html/.js         # formulario de ventas
  add-discount.html/.js     # formulario de descuentos
  styles.css / app.js       # estilos y lógica del panel
```

---

## 🚀 Backend (Java)

- **Requisitos**: Java 17 o superior.
- **Compilación**:
  ```powershell
  cd backend
  rm -rf bin && mkdir bin
  javac -d bin (Get-ChildItem -Recurse -Filter *.java src).FullName
  ```
- **Ejecución**:
  ```powershell
  java -cp bin com.kiosko.ServerApp
  ```
  Arranca un `HttpServer` en `http://localhost:8080`.

### Endpoints disponibles
| Método | Ruta               | Descripción |
|--------|--------------------|-------------|
| GET    | `/api/inventario`  | Devuelve todos los productos cargados (JSON). |
| POST   | `/api/productos`   | Alta de producto (`id`, `name`, `barcode`, `price`, `quantity`, `category`). Se persiste en `productos_kiosko.csv`. |
| GET    | `/api/ventas`      | Lista de ventas (una de ejemplo + las registradas vía formulario). |
| POST   | `/api/ventas`      | Crea una venta (`product`, `quantity`, `total`, `date` opcional). |
| GET    | `/api/descuentos`  | Descuentos automáticos + los ingresados manualmente. |
| POST   | `/api/descuentos`  | Alta de descuento (`name`, `type`, `condition`, `percentage`). |

Todas las respuestas son JSON y tienen CORS habilitado (`Access-Control-Allow-Origin: *`).

---

## 💻 Frontend (HTML/CSS/JS)

Servidor recomendado para desarrollo (elige uno y ejecútalo dentro de `frontend/`):
```powershell
python -m http.server 5500
# o
npx serve -l 5500
```

### Páginas
- `http://localhost:5500/index.html` → tablero principal con tablas y filtros.
- `http://localhost:5500/add-product.html` → formulario de productos (POST al backend).
- `http://localhost:5500/add-sale.html` → formulario de ventas.
- `http://localhost:5500/add-discount.html` → formulario de descuentos.

### Funcionalidades destacadas
- Tablas dinámicas con filtrado por texto (inventario, ventas, descuentos).
- Botones de acción que abren formularios dedicados (se ocultan filtros cuando no aplica).
- `app.js` controla navegación por pestañas y, por ahora, usa datos mock en el panel principal (los formularios sí consumen el backend real).

> **Importante:** mantener `ServerApp` corriendo en `localhost:8080` antes de usar los formularios, de lo contrario los `fetch` fallarán con `Failed to fetch`.

---

## ✅ Próximos pasos sugeridos
- Persistir ventas y descuentos custom en archivo o base de datos real.
- Actualizar `frontend/app.js` para que el tablero consuma la API real en lugar de datos simulados.
- Añadir autenticación/autorización básica antes de exponer el backend públicamente.
- Deploy del backend en algún servicio (Render, Railway, etc.) para que GitHub Pages pueda consumirlo.

---

¡Listo para subir a GitHub y seguir iterando! 💼🛒✨
