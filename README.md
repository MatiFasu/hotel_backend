# Agencia de Turismo & Hotel — Backend API

API RESTful transaccional construida con **Java 17** y **Spring Boot 3** para la gestión comercial y operativa de una agencia de turismo y hotelería. Diseñada bajo principios de arquitectura en capas, contratos DTO inmutables, migración de base de datos versionada y persistencia en **PostgreSQL / Supabase**.

---

## 1. ¿Qué hace? (Funcionalidades Principales)

El backend provee los servicios de negocio y persistencia para cubrir el ciclo comercial completo de la agencia:

- **Catálogo de Servicios Turísticos (`CU-03`):**
  - Gestión completa (alta, modificación, baja lógica y reactivación) de 7 tipos reglamentarios: `HOTEL`, `ALQUILER_AUTO`, `PASAJE_COLECTIVO`, `PASAJE_AVION`, `PASAJE_TREN`, `EXCURSION` y `ENTRADA_EVENTO`.
  - Validación de campos obligatorios, destinos, fechas futuras e importes monetarios mayores a cero.

- **Paquetes Turísticos Comerciales (`CU-05`):**
  - Armado de paquetes mediante la combinación de 2 o más servicios turísticos activos distintos.
  - Aplicación automática del **10% de descuento normativo** sobre la suma del costo regular de los servicios incluidos, calculado con precisión monetaria (`BigDecimal` y redondeo `HALF_UP`).

- **Padrón de Clientes (`CU-02`):**
  - Registro con unicidad de DNI.
  - **Reactivación automática inteligente:** si se intenta registrar un DNI que fue dado de baja previamente, el sistema reactiva el cliente y actualiza sus datos en una sola operación (`HTTP 200 OK`), evitando colisiones de clave única. Si ya está activo, responde `HTTP 409 Conflict`.
  - Búsqueda en vivo por DNI, nombre o apellido.

- **Nómina de Empleados y Protección Salarial (`CU-01`):**
  - Registro de personal con asignación de credenciales y roles (`ADMIN` o `VENDEDOR`).
  - **Protección Salarial (Decisión 17):** El endpoint de listado (`GET /api/empleados`) oculta las remuneraciones. El detalle (`GET /api/empleados/{id}`) solo expone el campo `sueldo` si el usuario autenticado posee rol `ADMIN` (verificado vía token JWT / header `X-User-Role: ADMIN`), protegiendo la confidencialidad salarial.

- **Punto de Venta y Snapshot Financiero (`CU-04`, `CU-06`, `CU-07`, `CU-08`):**
  - **Exclusión mutua estricta XOR:** una venta debe contener un servicio individual o un paquete turístico, nunca ambos ni ninguno.
  - **Deducción de comisiones bancarias por medio de pago:**
    - Efectivo: **0.00%**
    - Monedero Virtual: **0.00%**
    - Tarjeta de Débito: **3.00%**
    - Tarjeta de Crédito: **9.00%**
    - Transferencia Bancaria: **2.45%**
  - **Snapshot Financiero Inmutable:** La venta congela el nombre del producto, costo base, comisión porcentual, comisión monetaria y ganancia neta en el instante de la transacción para mantener auditoría histórica inalterable.
  - **Anulación de Ventas (`CU-09`):** Bajas lógicas con fecha de anulación que revierten el balance financiero de la agencia sin borrar el registro contable.

- **Reportes Financieros Consolidados — Bonus Point (`CU-10` & `CU-12`):**
  - **Arqueo Diario:** consolidación de ventas activas, comisiones y ganancia neta para una fecha específica.
  - **Balance Mensual con Serie Diaria Continua (Decisión 23):** genera los datos de todos los días calendario del mes (1 al 31); los días sin ventas se informan en `$0,00` con 0 operaciones para permitir gráficos de tendencia continuos sin discontinuidades.
  - Desglose contable agrupado por medio de pago.

- **Seguridad y Carga Demo (`CU-13`):**
  - Autenticación con emisión de tokens JWT firmados (`HS256`).
  - Endpoint de auto-poblado de base de datos (`POST /api/reportes/seed-demo`) para demostraciones rápidas.

---

## 2. ¿Cómo probarlo? (Instrucciones de Instalación y Ejecución)

### Prerrequisitos
- **Java 17 LTS** (OpenJDK / Eclipse Temurin).
- **Maven 3.9+** (o utilizar el wrapper incluido `./mvnw`).
- **PostgreSQL 16** local o instancia en la nube (ej. **Supabase**).
- Alternativamente: **Docker** y **Docker Compose**.

### Opción A: Ejecución con Docker Compose (Recomendada)
Desde la raíz del repositorio:
```bash
# Levantar PostgreSQL, Backend y Frontend de manera orquestada
docker compose up --build -d hotel-backend

# Verificar logs del servicio
docker compose logs -f hotel-backend
```
El backend estará disponible en `http://localhost:8080`.

### Opción B: Ejecución Local con Maven
1. Clonar el repositorio y posicionarse en la carpeta `backend`:
   ```bash
   cd backend
   ```
2. Configurar las variables de conexión a la base de datos en `src/main/resources/application.properties` o mediante variables de entorno:
   ```properties
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/agencia_turismo_db
   SPRING_DATASOURCE_USERNAME=agencia_user
   SPRING_DATASOURCE_PASSWORD=agencia_password_secret
   ```
   *(Si se utiliza Supabase con IPv4, conectar a través del Transaction/Session Pooler en puerto 5432 / 6543 con `sslmode=require`).*

3. Iniciar la aplicación con el perfil demo activado (puebla usuarios y datos semilla automáticamente):
   ```bash
   # En Linux / macOS:
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=demo

   # En Windows (PowerShell):
   .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=demo
   ```

### Credenciales de Prueba (Perfil Demo)
| Rol | Usuario (`username`) | Contraseña (`password`) | Permisos |
|---|---|---|---|
| **ADMIN** | `admin` | `Admin1234!` | Acceso total, visualización salarial y reportes |
| **VENDEDOR** | `vendedor` | `Vendedor1234!` | Catálogo, ventas, clientes y arqueos diarios |

---

## 3. ¿Cómo lo construí? (Tecnologías Usadas y Decisiones Técnicas Relevantes)

### Stack Tecnológico
- **Lenguaje:** Java 17 LTS.
- **Framework Principal:** Spring Boot 3.2.3.
- **Ecosistema Spring:** Spring Data JPA, Spring Security, Spring Validation (Jakarta Bean Validation), Spring Actuator.
- **Persistencia & ORM:** Hibernate 6 + PostgreSQL 16 Driver.
- **Control de Versiones de BD:** Flyway Migration (`V1__init_schema.sql` con validación DDL estricta `ddl-auto=validate`).
- **Seguridad:** Tokens JWT (Java JWT `jjwt-api 0.12.3`).
- **Control de Tiempo:** Inyección de Bean `Clock` configurado en `ZoneId.of("America/Argentina/Buenos_Aires")` para garantizar consistencia horaria oficial en todas las transacciones.

### Decisiones Técnicas Relevantes
1. **Arquitectura Limpia en Capas y Aislamiento DTO:**
   - Se aplicó una separación estricta entre capa `Controller`, `Service` y `Repository`.
   - Las entidades JPA nunca se exponen directamente a los endpoints REST; todas las comunicaciones entrantes y salientes se gestionan mediante DTOs inmutables con validaciones Jakarta (`@NotNull`, `@Positive`, `@Size`, etc.).
2. **Exclusión Mutua XOR en Ventas (Decisiones 7 y 8):**
   - Se implementó una regla de negocio que garantiza que una venta contenga exactamente un servicio individual o un paquete turístico (`servicioId != null ^ paqueteId != null`).
   - El snapshot financiero congela montos y descripciones al instante de la venta para que futuras ediciones de precios en el catálogo no alteren balances contables pasados.
3. **Cálculo Financiero con Precisión `BigDecimal`:**
   - Todos los cálculos de precios, comisiones y descuentos del 10% se realizan con `BigDecimal` y modo de redondeo `RoundingMode.HALF_UP` a 2 decimales, previniendo errores de punto flotante binario (`double`).
4. **Manejo Centralizado de Excepciones (`@RestControllerAdvice`):**
   - Respuestas de error estructuradas con formato RFC 7807 (`status`, `error`, `message`, `path`, `timestamp`), mapeando validaciones a `400 Bad Request`, colisiones a `409 Conflict` y accesos indebidos a `403 Forbidden`.
5. **Estrategia de Bajas Lógicas:**
   - Los registros de clientes, empleados, servicios y paquetes no se eliminan físicamente (`DELETE`), sino que se marcan con `activo = false` para preservar la integridad referencial histórica de ventas pasadas.

---

## 4. ¿Cómo verificarlo? (Instrucciones para Ejecutar los Tests)

El backend cuenta con una suite integral de **65 tests automatizados** que cubren pruebas unitarias, de integración y de capa web.

### Ejecución de Pruebas
Ejecutar la suite completa mediante Maven:
```bash
# En Linux / macOS:
./mvnw test

# En Windows (PowerShell):
.\mvnw.cmd test
```

### Cobertura de la Suite de Tests
- **Tests de Controladores (`MockMvc`):**
  - Autenticación JWT y rechazo de credenciales inválidas.
  - Validación de payloads HTTP (campos requeridos, tipos de dato, límites de caracteres).
  - Verificación de códigos de respuesta (`200 OK`, `201 Created`, `400 Bad Request`, `404 Not Found`, `409 Conflict`).
- **Tests de Servicios de Negocio (`Mockito`):**
  - Validación de la regla XOR en emisión de ventas (rechazo de ventas con ambos ítems o ninguno).
  - Cálculo exacto del 10% de descuento en armado de paquetes de 2 o más servicios.
  - Lógica de reactivación automática por DNI inactivo en clientes.
  - Ocultamiento y visibilidad condicional de sueldos en empleados según rol `ADMIN` vs `VENDEDOR`.
  - Generación de la serie continua mensual con relleno en `$0,00` para días sin actividad.
- **Tests de Integración y Repositorios:**
  - Consultas JPA personalizadas, filtros de listados y transaccionalidad (`@Transactional`).
