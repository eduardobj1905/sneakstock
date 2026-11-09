# Despliegue

SneakStock se despliega como **una sola imagen Docker** (el `Dockerfile` de la raíz): el frontend compilado se sirve desde Spring Boot, así que solo hay que publicar un servicio y conectarlo a una base de datos MySQL. Flyway crea las tablas al arrancar.

La configuración se pasa con variables de entorno:

| Variable | Ejemplo | Para qué |
|---|---|---|
| `DB_URL` | `jdbc:mysql://host:puerto/basedatos?sslMode=REQUIRED` | Conexión a MySQL |
| `DB_USERNAME` | `avnadmin` | Usuario de MySQL |
| `DB_PASSWORD` | *(la del proveedor)* | Contraseña de MySQL. Nunca en el repositorio |
| `PORT` | `8080` | Puerto; muchas plataformas lo ponen solas |

## Opción propuesta: Render (app) + Aiven (MySQL)

Las dos tienen un plan gratuito pensado para proyectos personales. Revisa sus condiciones actuales antes de empezar, porque cambian con el tiempo.

### 1. Base de datos en Aiven

1. Crea una cuenta en [aiven.io](https://aiven.io) y un servicio **MySQL** con el plan gratuito.
2. En la página del servicio copia **Host**, **Port**, **User**, **Password** y **Database name** (`defaultdb`).
3. La URL de conexión queda así:
   `jdbc:mysql://HOST:PORT/defaultdb?sslMode=REQUIRED`

### 2. La app en Render

1. Crea una cuenta en [render.com](https://render.com) con tu usuario de GitHub.
2. **New → Web Service** → elige el repositorio `sneakstock`. Render detecta el `Dockerfile`.
3. Plan **Free**. En **Environment** añade `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` con los datos de Aiven.
4. **Deploy**. La primera compilación tarda varios minutos. Al terminar, la app queda en `https://<nombre>.onrender.com`.

En el plan gratuito, el servicio se duerme tras un rato sin visitas: la primera petición después tarda unos segundos en despertar.

### 3. Datos de ejemplo

Con la app ya desplegada una vez (para que Flyway haya creado las tablas), carga los datos de ejemplo desde **Git Bash**, en la raíz del repositorio:

```bash
docker run --rm -i mysql:8.4 mysql --default-character-set=utf8mb4 --ssl-mode=REQUIRED \
  -h HOST -P PORT -u USUARIO -pCONTRASEÑA defaultdb < docs/datos-ejemplo.sql
```

Se usa Git Bash y no PowerShell porque PowerShell 5 cambia la codificación al pasar el archivo y se perderían las tildes.

## Probar la imagen en local

Antes de desplegar, la misma imagen se puede probar con Docker Compose (MySQL incluido):

```bash
docker compose --profile app up --build
```

La app queda en `http://localhost:8081`.

## Ojo: la demo no tiene login

Cualquiera que tenga el enlace puede registrar ventas o entradas en la demo. Para un portfolio basta con volver a cargar los datos de ejemplo si alguien los cambia. El siguiente paso previsto es el login con roles (Spring Security + JWT).
