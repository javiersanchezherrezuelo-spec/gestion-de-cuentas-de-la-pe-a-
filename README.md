# Peña Los Informales 🍺

Web para gestionar las cuentas de la peña: cada miembro se apunta lo que consume desde el móvil,
los administradores controlan productos, precios y stock, y la web avisa cuando algo se acaba.

- **Backend:** Java 21 + Spring Boot 4 + MySQL (`backend/`)
- **Frontend:** React + Vite, instalable en el móvil (PWA) (`frontend/`)

## Arrancar en local

Requisitos: Java 21 o superior, Node 20 o superior y MySQL en marcha.

```bash
# 1. Backend (crea la base de datos pena_informales automáticamente)
cd backend
DB_PASSWORD=tu_password_de_mysql ./mvnw spring-boot:run
```

```bash
# 2. Frontend (en otra terminal)
cd frontend
npm install
npm run dev
```

Abre http://localhost:5173 y entra con el administrador inicial: usuario `admin`, contraseña `admin1234`.
**Cámbiala nada más entrar.**

## Variables de entorno

| Variable | Para qué sirve | Por defecto en local |
|---|---|---|
| `DB_URL` | URL JDBC de MySQL | `jdbc:mysql://localhost:3306/pena_informales` |
| `DB_USER` / `DB_PASSWORD` | Usuario y contraseña de MySQL | `root` / vacío |
| `JWT_SECRET` | Clave para firmar las sesiones (larga y aleatoria) | una de ejemplo |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | Admin que se crea la primera vez | `admin` / `admin1234` |

## Tests

```bash
cd backend
./mvnw test
```
