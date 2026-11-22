# Ferretería Los Maestros — Backend (monolito Spring Boot)

Backend REST/JSON del proyecto semestral **DSY1104 · Desarrollo FullStack II** (Forma E).
Arquitectura **monolito** con patrón **CSR (Controller–Service–Repository)** por paquete de dominio,
**PostgreSQL**, **JWT + roles**, **Swagger**, **HATEOAS** y **Flyway**.

| Componente | Versión |
|---|---|
| Java | 21 (LTS) |
| Spring Boot | 4.1.2 |
| PostgreSQL | 16 |
| springdoc-openapi | 3.2.0 |
| jjwt | 0.13.0 |
| MapStruct | 1.6.3 |

---

## 1. Requisitos

- **JDK 21** (`java -version`).
- **Docker** (para la base de datos local) **o** un PostgreSQL 16 instalado.
- No hace falta instalar Maven: el proyecto incluye el *wrapper* (`./mvnw`).

## 2. Levantar en local

```bash
# 1) Base de datos (PostgreSQL 16 en localhost:5432, BD ferreteria_db)
docker compose up -d

# 2) Backend (Flyway crea el esquema y el seed carga roles/usuarios/catálogo)
./mvnw spring-boot:run
```

- API: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Health: <http://localhost:8080/actuator/health>

Desde IntelliJ: ejecutar la clase `com.ferreteria.maestros.FerreteriaMaestrosApplication`
(no requiere variables de entorno en local: los valores por defecto apuntan a PostgreSQL en `localhost:5432`).

## 3. Usuarios demo

Todos con contraseña **`1234`** (cargados por `config/DataSeeder`):

| Correo | Rol |
|---|---|
| `sebas@duoc.cl` | Administrador |
| `edi@profesor.duoc.cl` | Vendedor |
| `maria.torres@gmail.com` | Cliente |
| `carlos.rojas@duoc.cl` | Cliente |
| `jose.fernandez@gmail.com` | Vendedor |

## 4. Variables de entorno (producción / Render)

En local no se necesitan. En el servicio de Render se definen:

| Variable | Descripción |
|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC de PostgreSQL (`jdbc:postgresql://host:5432/bd`). |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la BD. |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la BD. |
| `JWT_SECRET` | Clave de firma de los JWT (mínimo 32 caracteres). |
| `JWT_EXPIRATION_MS` | Vigencia del token en ms (por defecto 2 h). |
| `APP_CORS_ALLOWED_ORIGINS` | Origen(es) del front separados por coma (dominio de Vercel). |
| `PORT` | Lo inyecta Render; el `application.yaml` lo usa con `${PORT:8080}`. |

## 5. Tests

```bash
./mvnw test
```

Todos los tests corren **sin base de datos**: los de servicio son unitarios (Mockito/JUnit) y el
smoke test del contexto usa el perfil `test` con **H2 en memoria** (`src/test/resources/application-test.yaml`).

## 6. Estructura

```
src/main/java/com/ferreteria/maestros/
├── auth/         # login, registro, sesión (JWT)
├── usuario/      # usuarios, roles, historial
├── producto/     # categorías, subcategorías, productos (+ StockServicePort)
├── carrito/      # carrito persistente
├── pedido/       # checkout, estados, boleta (+ PedidoQueryPort)
├── cuenta/       # cuentas corrientes y movimientos (+ CuentaCorrientePort)
├── contacto/     # mensajes de contacto
├── reporte/      # métricas del negocio
├── config/       # seguridad, CORS, Swagger, seeder
└── common/       # errores, paginación, utilidades
src/main/resources/
├── application.yaml
└── db/migration/ # Flyway (V1 esquema, V2 catálogo)
```

## 7. Documentación

- Diseño y contratos: `../02_Backend_Monolito_Entidades_Endpoints.md`
- Reparto por integrante: `../04_Distribucion_Backend_Integrantes.md`
- Bitácora de cambios y decisiones: `../07_Bitacora_Backend.md`
