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

## Probarla en un ordenador con Linux (Docker)

La app y MySQL arrancan juntas con Docker Compose. Las instrucciones son para Ubuntu o Debian.

```bash
# 1. Instalar Docker (solo la primera vez) y cerrar sesión y volver a entrar
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER

# 2. Descargar el proyecto
git clone https://github.com/javiersanchezherrezuelo-spec/gestion-de-cuentas-de-la-pe-a-.git
cd gestion-de-cuentas-de-la-pe-a-

# 3. Crear el archivo .env con claves aleatorias (cambia la contraseña del admin y el email)
cat > .env <<EOF
DB_PASSWORD=$(openssl rand -hex 16)
DB_ROOT_PASSWORD=$(openssl rand -hex 16)
JWT_SECRET=$(openssl rand -hex 32)
ADMIN_PASSWORD=pon-aqui-tu-contraseña
APP_CONTACTO=mailto:tu-email@gmail.com
EOF

# 4. Construir y arrancar (la primera vez tarda unos minutos)
docker compose up -d --build
```

- Desde el propio ordenador: http://localhost:8080
- Desde el móvil, conectado a la misma wifi: `http://IP-DEL-ORDENADOR:8080`. La IP la ves con `hostname -I`.
  Si no carga, abre el puerto con `sudo ufw allow 8080`.

**HTTPS y acceso desde fuera de casa.** Las notificaciones y la instalación en el móvil necesitan `https://`.
Cloudflare da una dirección HTTPS gratis, sin cuenta:

```bash
docker compose --profile tunel up -d
docker compose logs tunel | grep trycloudflare
```

Esa dirección (`https://algo.trycloudflare.com`) cambia cada vez que se reinicia el túnel.
Si cambia, cada uno tiene que volver a activar las notificaciones. Para algo definitivo hace falta un dominio propio.

**Comandos útiles**

| Para… | Comando |
|---|---|
| Ver los logs | `docker compose logs -f app` |
| Parar | `docker compose down` (los datos se conservan) |
| Actualizar a la última versión | `git pull && docker compose up -d --build` |
| Copia de seguridad | `docker compose exec db sh -c 'mysqldump -u root -p"$MYSQL_ROOT_PASSWORD" pena_informales' > copia.sql` |

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
