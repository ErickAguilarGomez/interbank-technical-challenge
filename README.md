# Reto Técnico: Arquitectura Batch & Event-Driven

## 1. Microservicios

| Módulo / Microservicio | Responsabilidad | Puerto |
| :--- | :--- | :---: |
| **`common`** | Entidad JPA (`TransactionRecord`), DTOs inmutables, enums y repositorios compartidos. | Librería interna |
| **`servicio-a`** | **Batch & Producer**: Ingesta masiva en chunks (50 registros) con Spring Batch y publicación en Kafka. | `127.0.0.1:8081` |
| **`servicio-b`** | **Consumer & Worker**: Suscrito al tópico de Kafka, procesa validaciones en 3 hilos y actualiza a `COMPLETED`/`FAILED`. | Red interna Docker (`8082`) |
| **`servicio-c`** | **API de Monitoreo**: Endpoints REST de solo lectura para consultar estado general y métricas. No conecta a Kafka. | `0.0.0.0:8083` |

---

## 2. Configuración de Variables de Entorno (`.env`)

El proyecto incluye un archivo `.env.example` preconfigurado. Para inicializar el entorno:

```bash
cp .env.example .env
```

### Detalle de Variables:

| Variable | Valor por Defecto | Descripción |
| :--- | :--- | :--- |
| `POSTGRES_DB` | `bankdb` | Nombre de la base de datos relacional. |
| `POSTGRES_USER` | `postgres` | Usuario administrador de PostgreSQL. |
| `POSTGRES_PASSWORD` | `postgres` | Contraseña de acceso a la base de datos. |
| `POSTGRES_PORT` | `5432` | Puerto en el host vinculado a PostgreSQL (`127.0.0.1:5432`). |
| `DB_PORT` | `5432` | Puerto interno de conexión hacia la base de datos. |
| `KAFKA_PORT` | `9092` | Puerto en el host vinculado a Kafka (`127.0.0.1:9092`). |
| `KAFKA_CLUSTER_ID` | `MkU3OEVBNTcwNTJENDM2Qk` | Identificador único del cluster Kafka KRaft. |
| `SERVICIO_A_PORT` | `8081` | Puerto HTTP del Servicio A (`127.0.0.1:8081`). |
| `SERVICIO_B_PORT` | `8082` | Puerto interno del Servicio B (no expuesto al host). |
| `SERVICIO_C_PORT` | `8083` | Puerto HTTP público del Servicio C (`0.0.0.0:8083`). |
| `JAVA_OPTS` | `-XX:+UseG1GC -XX:MaxRAMPercentage=75.0` | Parámetros de memoria y GC para contenedores. |

---

## 3. Despliegue con Docker Compose

```bash
# 1. Compilar el proyecto multi-módulo (requiere Java 17)
export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || echo $JAVA_HOME)
mvn clean package -DskipTests

# 2. Construir e iniciar contenedores en segundo plano
docker compose up --build -d

# 3. Verificar estado de los contenedores
docker compose ps
```

---

## 4. Endpoints y Pruebas

### 4.1. Iniciar Procesamiento Batch (Servicio A)
```bash
curl -X POST http://127.0.0.1:8081/api/v1/batch/start
```

Consultar estado del job batch:
```bash
curl http://127.0.0.1:8081/api/v1/batch/status
```

### 4.2. Monitoreo (Servicio C)

- **Conteo por estado**:
```bash
curl http://localhost:8083/api/v1/monitoring/status
```

- **Métricas de rendimiento**:
```bash
curl http://localhost:8083/api/v1/monitoring/metrics
```

- **Listado paginado de transacciones**:
```bash
curl "http://localhost:8083/api/v1/monitoring/records?page=0&size=5&status=COMPLETED"
```

---

## 5. Justificación Técnica: Latencia y Rendimiento

- **Optimizaciones JPA**: Inserciones agrupadas en PostgreSQL con `hibernate.jdbc.batch_size=50`, `order_inserts=true` y `GenerationType.SEQUENCE` (`allocationSize=50`), evitando peticiones individuales fila por fila.
- **Configuración Kafka (Productor)**: `acks=all` para consistencia bancaria estricta, compresión `snappy`, `linger.ms=20` y búfer de 32 KB para empaquetar eventos y maximizar throughput.
- **Configuración Kafka (Consumidor)**: 3 hilos concurrentes (`concurrency=3`) asignados a las 3 particiones y `manual_immediate` ack tras confirmar la persistencia en base de datos (*at-least-once*).
- **Desacoplamiento Asíncrono**: `servicio-a` despacha a Kafka mediante callbacks no bloqueantes y continúa de inmediato con el siguiente lote sin esperar respuesta de `servicio-b`.

---

## 6. Pruebas Automatizadas (JUnit 5 & Mockito)

Ejecución de la suite completa de pruebas:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || echo $JAVA_HOME)
mvn test
```