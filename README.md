# Peña Los Informales 🍻

Web para llevar las cuentas de la peña desde el móvil:

- Cada miembro entra con su usuario y **se apunta lo que toma** (cervezas, Coca-Colas, Fantas…).
- Lo apuntado se suma a su **deuda**, que paga más tarde; un admin apunta el pago.
- La pestaña **Resumen** muestra a todos las deudas y el stock.
- Los admins gestionan **productos, precios, stock, miembros y pagos**.
- **Avisos**: cuando un producto llega a su mínimo (a los admins) y el día 1 de cada mes (a cada uno, con su deuda).
  El admin elige los canales en *Admin → Avisos*: notificaciones en el móvil, email y botón para compartir por WhatsApp.

| Parte | Tecnología | Carpeta |
|---|---|---|
| Backend | Java 21, Spring Boot 4, Spring Security (JWT), JPA, MySQL | `backend/` |
| Frontend | React + Vite, instalable en el móvil (PWA) | `frontend/` |

## Arrancar en local

Necesitas Java 21 o superior, Node 20 o superior y MySQL en marcha.

```bash
cd backend
DB_PASSWORD=tu_password_de_mysql ./mvnw spring-boot:run
```

La base de datos `pena_informales` se crea sola. En otra terminal:

```bash
cd frontend
npm install
npm run dev
```

Abre http://localhost:5173 y entra con el admin inicial: usuario `admin`, contraseña `admin1234`.
**Cámbiala nada más entrar** (en *Mi cuenta*).

<details>
<summary>¿Sin MySQL? Arrancar con una base de datos en memoria (se borra al parar)</summary>

```bash
cd backend
DB_URL="jdbc:h2:mem:pena;MODE=MySQL;DB_CLOSE_DELAY=-1" DB_USER=sa ./mvnw spring-boot:run -Dspring-boot.run.useTestClasspath=true
```

</details>

## Tests

```bash
cd backend
./mvnw test
```

## Variables de entorno

| Variable | Para qué sirve | Por defecto |
|---|---|---|
| `DB_URL` | URL JDBC de MySQL | `jdbc:mysql://localhost:3306/pena_informales…` |
| `DB_USER` / `DB_PASSWORD` | Usuario y contraseña de MySQL | `root` / vacío |
| `JWT_SECRET` | Clave para firmar las sesiones. **En el servidor, pon una larga y aleatoria** | una de ejemplo |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | Admin que se crea la primera vez (solo si no hay usuarios) | `admin` / `admin1234` |
| `APP_CONTACTO` | Email de contacto para las notificaciones push (`mailto:…`). Apple lo exige | `mailto:pena.informales@example.com` |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | Servidor de correo para los avisos por email | sin configurar: los correos se escriben en el log |
| `PORT` | Puerto del servidor | `8080` |

### Avisos por email

Con Gmail: activa la verificación en dos pasos y crea una
[contraseña de aplicación](https://myaccount.google.com/apppasswords). Después:

```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=lapena@gmail.com
MAIL_PASSWORD=la-contraseña-de-aplicación
```

También sirve [Brevo](https://www.brevo.com/) (gratis hasta 300 correos al día) con `MAIL_HOST=smtp-relay.brevo.com`.
Cada miembro pone su email en *Mi cuenta*.

### Notificaciones en el móvil

No hay que configurar nada: las claves se generan solas y se guardan en la base de datos.
Cada miembro las activa en *Mi cuenta → Activar notificaciones*.

- La web tiene que estar publicada con **HTTPS** (en local vale `localhost`).
- **En iPhone** (iOS 16.4 o posterior) primero hay que añadir la web a la pantalla de inicio:
  *Compartir → Añadir a pantalla de inicio*, y abrirla desde ese icono.

## Publicarla en internet

El `Dockerfile` construye la web y el backend en una sola imagen. Cualquier hosting con Docker sirve.
Por ejemplo, con [Railway](https://railway.com) (unos 5 $/mes):

1. Crea un proyecto nuevo desde este repositorio de GitHub. Railway detecta el `Dockerfile`.
2. Añade una base de datos **MySQL** al proyecto.
3. En las variables del servicio de la web, pon:
   - `DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}`
   - `DB_USER=${{MySQL.MYSQLUSER}}` y `DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}`
   - `JWT_SECRET`, `ADMIN_PASSWORD` y `APP_CONTACTO`, más los de email si lo usáis.
4. En *Settings → Networking*, genera un dominio. Ya viene con HTTPS.
