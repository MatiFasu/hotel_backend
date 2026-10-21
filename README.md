# Agencia de Turismo & Hotel — Backend API (HackaCode 2da Edición)

API RESTful transaccional para la comercialización de servicios turísticos individuales y paquetes turísticos con descuento automático, padrón de clientes y empleados con control salarial, punto de venta con exclusión mutua XOR, snapshot financiero y reportes de ganancias netas.

---

## 1. Stack Tecnológico

- **Lenguaje & Runtime:** Java 17 LTS
- **Framework:** Spring Boot 3.2.3 (Web, Data JPA, Validation, Actuator)
- **Base de Datos:** PostgreSQL 16 (`spring.jpa.hibernate.ddl-auto=validate`)
- **Migraciones DDL:** Flyway (`V1__init_schema.sql`)
- **Zona Horaria Oficial:** `America/Argentina/Buenos_Aires` inyectada vía Bean `Clock`
- **Testing:** JUnit 5, Mockito, MockMvc y Testcontainers / PostgreSQL 16 (**65 tests automatizados**)

---

## 2. Configuración de Base de Datos (Para Replicar Entorno)

| Parámetro | Valor por Defecto | Variable de Entorno |
|---|---|---|
| **Motor** | PostgreSQL 16 | — |
| **Host / Puerto** | `localhost:5432` | `SPRING_DATASOURCE_URL` |
| **Base de Datos** | `agencia_turismo_db` | `POSTGRES_DB` |
| **Usuario** | `agencia_user` | `SPRING_DATASOURCE_USERNAME` |
| **Contraseña** | `agencia_password_secret` | `SPRING_DATASOURCE_PASSWORD` |
| **Perfil Demo (Auto-Seed)** | `demo` | `SPRING_PROFILES_ACTIVE=demo` |

El esquema relacional e índices se crean automáticamente al iniciar la aplicación mediante Flyway (`src/main/resources/db/migration/V1__init_schema.sql`).

---

## 3. Credenciales Semilla de Demostración (Login JWT)

Al iniciar con `SPRING_PROFILES_ACTIVE=demo` (o invocando `POST /api/reportes/seed-demo`), el sistema crea dos usuarios de prueba:

| Rol | Usuario (`username`) | Contraseña (`password`) | Permisos Destacados |
|---|---|---|---|
| **ADMIN** | `admin` | `Admin1234!` | Acceso total, visualización de `sueldo` en detalle de empleados y gestión completa |
| **VENDEDOR** | `vendedor` | `Vendedor1234!` | Gestión comercial de catálogo, clientes y emisión de ventas |

---

## 4. Endpoints Principales

- **Autenticación (`CU-13`):**
  - `POST /api/auth/login` — Valida credenciales y emite token JWT (`HS256`).
- **Servicios Turísticos (`CU-03`):**
  - `GET /api/servicios`, `POST /api/servicios`, `PUT /api/servicios/{id}`, `DELETE /api/servicios/{id}` (baja lógica), `PATCH /api/servicios/{id}/reactivar`.
- **Paquetes Turísticos (`CU-05`):**
  - `GET /api/paquetes`, `POST /api/paquetes` (mínimo 2 servicios distintos y activos, calcula **10% de descuento** con `HALF_UP`), `PUT /api/paquetes/{id}`, `DELETE /api/paquetes/{id}`, `PATCH /api/paquetes/{id}/reactivar`.
- **Clientes (`CU-02`):**
  - `GET /api/clientes`, `POST /api/clientes` (`201 Created` en alta nueva, `200 OK` en reactivación automática por DNI inactivo, `409 Conflict` si DNI activo), `PUT /api/clientes/{id}`, `DELETE /api/clientes/{id}`, `PATCH /api/clientes/{id}/reactivar`.
- **Empleados (`CU-01`):**
  - `GET /api/empleados` (oculta `sueldo`), `GET /api/empleados/{id}` (expone `sueldo` solo con header `X-User-Role: ADMIN`), `POST /api/empleados`, `PUT /api/empleados/{id}`, `DELETE /api/empleados/{id}`, `PATCH /api/empleados/{id}/reactivar`.
- **Medios de Pago y Ventas (`CU-04`, `CU-06`, `CU-07`, `CU-08`, `CU-09`):**
  - `GET /api/medios-pago` — Tasas oficiales: Efectivo (0%), Monedero Virtual (0%), Tarjeta Débito (3%), Tarjeta Crédito (9%), Transferencia (2.45%).
  - `POST /api/ventas` — Exclusión mutua XOR (`servicioId` o `paqueteId`) y snapshot financiero inmutable.
  - `GET /api/ventas` y `PATCH /api/ventas/{id}/anular`.
- **Reportes Financieros — Bonus Point (`CU-10` & `CU-12`):**
  - `GET /api/reportes/ganancias-diarias?fecha=YYYY-MM-DD`
  - `GET /api/reportes/ganancias-mensuales?anio=YYYY&mes=MM` (incluye serie diaria continua de todo el mes).
  - `POST /api/reportes/seed-demo`

---

## 5. Ejecución Local y Pruebas

```bash
# Ejecutar la suite completa de 65 tests
./mvnw test

# Iniciar el servidor en puerto 8080
./mvnw spring-boot:run
```
