# ✅ Implementación Backend - Módulo de Facturación

## 📦 Archivos Creados

### 1. Migración de Base de Datos
- ✅ `/src/main/resources/db/migration/V48__facturacion_papel.sql`
  - Tabla `financiero.timbrado`
  - Tabla `financiero.factura`
  - Tabla `financiero.detalle_factura`
  - Ampliación de `personas.empresa`
  - Datos de ejemplo iniciales

### 2. Entidades JPA (Domain)
- ✅ `/src/main/java/com/dev/ultron/domain/financiero/Timbrado.java`
- ✅ `/src/main/java/com/dev/ultron/domain/financiero/Factura.java`
- ✅ `/src/main/java/com/dev/ultron/domain/financiero/DetalleFactura.java`

### 3. DTOs Input
- ✅ `/src/main/java/com/dev/ultron/dto/financiero/input/TimbradoInput.java`
- ✅ `/src/main/java/com/dev/ultron/dto/financiero/input/FacturaInput.java`
- ✅ `/src/main/java/com/dev/ultron/dto/financiero/input/DetalleFacturaInput.java`

### 4. DTOs Output
- ✅ `/src/main/java/com/dev/ultron/dto/financiero/output/TimbradoOutput.java`
- ✅ `/src/main/java/com/dev/ultron/dto/financiero/output/FacturaOutput.java`
- ✅ `/src/main/java/com/dev/ultron/dto/financiero/output/DetalleFacturaOutput.java`

### 5. Mappers (MapStruct)
- ✅ `/src/main/java/com/dev/ultron/dto/financiero/mapper/TimbradoMapper.java`
- ✅ `/src/main/java/com/dev/ultron/dto/financiero/mapper/FacturaMapper.java`
- ✅ `/src/main/java/com/dev/ultron/dto/financiero/mapper/DetalleFacturaMapper.java`

### 6. Repositories
- ✅ `/src/main/java/com/dev/ultron/repository/financiero/TimbradoRepository.java`
- ✅ `/src/main/java/com/dev/ultron/repository/financiero/FacturaRepository.java`
- ✅ `/src/main/java/com/dev/ultron/repository/financiero/DetalleFacturaRepository.java`

### 7. Services
- ✅ `/src/main/java/com/dev/ultron/service/financiero/TimbradoService.java`
- ✅ `/src/main/java/com/dev/ultron/service/financiero/FacturaService.java`

### 8. GraphQL Schema
- ✅ `/src/main/resources/graphql/financiero/facturacion.graphqls`

### 9. Controlador GraphQL
- ✅ `/src/main/java/com/dev/ultron/controller/financiero/FacturacionGraphQLController.java`

### 10. Documentación
- ✅ `/FACTURACION_GRAPHQL_EJEMPLOS.md`

---

## 🎯 Funcionalidades Implementadas

### Gestión de Timbrados
- ✅ Registrar timbrado
- ✅ Actualizar timbrado
- ✅ Listar timbrados por empresa
- ✅ Listar timbrados activos
- ✅ Obtener timbrado disponible
- ✅ Activar/desactivar timbrado
- ✅ Eliminar timbrado
- ✅ Validación de vigencia
- ✅ Cálculo de números disponibles
- ✅ Cálculo de días hasta vencimiento

### Gestión de Facturas
- ✅ Emitir factura
- ✅ Generación automática de número secuencial
- ✅ Validación de timbrado activo y vigente
- ✅ Cálculo automático de IVA (5%, 10%, EXENTA)
- ✅ Cálculo automático de totales
- ✅ Snapshot de datos del cliente
- ✅ Anular factura (con motivo y auditoría)
- ✅ Consultar factura por ID con detalles
- ✅ Buscar facturas con filtro
- ✅ Listar facturas por estado
- ✅ Listar facturas por cliente
- ✅ Obtener facturas para libro de ventas
- ✅ Paginación de resultados
- ✅ Control de concurrencia en numeración

---

## 🔒 Características de Seguridad

- ✅ Numeración secuencial con control de concurrencia (synchronized)
- ✅ Validación de timbrado activo y vigente antes de emitir
- ✅ Validación de números disponibles
- ✅ Snapshot de datos (inmutabilidad histórica)
- ✅ Auditoría de usuario emisor
- ✅ Auditoría de usuario que anula
- ✅ Anulación irreversible
- ✅ Timestamp automático de todas las operaciones

---

## 🧮 Lógica de Negocio Implementada

### 1. Generación de Número de Factura
```
Formato: XXX-XXX-XXXXXXX
Ejemplo: 001-001-0000123

establecimiento-punto_expedicion-numero_secuencial
```

### 2. Cálculo de IVA
```java
IVA 5%:  monto_iva = subtotal × 0.05 / 1.05
IVA 10%: monto_iva = subtotal × 0.10 / 1.10
EXENTA:  monto_iva = 0
```

### 3. Validaciones Críticas
- Timbrado debe estar activo
- Timbrado debe estar vigente (fecha actual entre inicio y fin)
- Timbrado debe tener números disponibles
- Factura debe tener al menos un detalle
- Motivo de anulación es obligatorio
- No se puede anular una factura ya anulada

---

## 📊 Estructura de Base de Datos

### Tabla: financiero.timbrado
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id_timbrado | BIGSERIAL | PK |
| numero_timbrado | VARCHAR(20) | Número de timbrado SET |
| establecimiento | VARCHAR(3) | Código establecimiento |
| punto_expedicion | VARCHAR(3) | Código punto de expedición |
| numero_inicial | INTEGER | Primer número autorizado |
| numero_final | INTEGER | Último número autorizado |
| numero_actual | INTEGER | Próximo número a emitir |
| fecha_inicio_vigencia | DATE | Inicio de vigencia |
| fecha_fin_vigencia | DATE | Fin de vigencia |
| id_empresa | BIGINT | FK a empresa |
| tipo_factura | VARCHAR(20) | PAPEL/ELECTRONICA |
| activo | BOOLEAN | Estado del timbrado |

### Tabla: financiero.factura
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id_factura | BIGSERIAL | PK |
| numero_factura | VARCHAR(20) | Número de factura |
| timbrado | VARCHAR(20) | Número de timbrado (desnormalizado) |
| fecha_emision | TIMESTAMP | Fecha de emisión |
| id_timbrado | BIGINT | FK a timbrado |
| id_cliente | BIGINT | FK a cliente (opcional) |
| id_venta | BIGINT | FK a venta (opcional) |
| id_sesion_caja | BIGINT | FK a sesión de caja (opcional) |
| id_empresa | BIGINT | FK a empresa |
| cliente_nombre | VARCHAR(255) | Snapshot nombre cliente |
| cliente_documento | VARCHAR(20) | Snapshot documento |
| cliente_ruc | VARCHAR(20) | Snapshot RUC |
| cliente_direccion | VARCHAR(500) | Snapshot dirección |
| subtotal | NUMERIC(19,2) | Subtotal de la factura |
| total_iva_5 | NUMERIC(19,2) | Total IVA 5% |
| total_iva_10 | NUMERIC(19,2) | Total IVA 10% |
| total_exenta | NUMERIC(19,2) | Total exenta |
| total_iva | NUMERIC(19,2) | Total IVA |
| total | NUMERIC(19,2) | Total de la factura |
| forma_pago | VARCHAR(50) | Forma de pago |
| moneda | VARCHAR(3) | Moneda |
| estado | VARCHAR(20) | EMITIDA/ANULADA |
| fecha_anulacion | TIMESTAMP | Fecha de anulación |
| motivo_anulacion | TEXT | Motivo de anulación |
| id_usuario_anulacion | BIGINT | Usuario que anuló |
| observaciones | TEXT | Observaciones |
| id_usuario_emisor | BIGINT | Usuario emisor |
| fecha_creacion | TIMESTAMP | Fecha de creación |

### Tabla: financiero.detalle_factura
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id_detalle_factura | BIGSERIAL | PK |
| id_factura | BIGINT | FK a factura |
| id_producto | BIGINT | FK a producto (opcional) |
| id_servicio | BIGINT | FK a servicio (opcional) |
| id_presentacion | BIGINT | FK a presentación (opcional) |
| descripcion | VARCHAR(500) | Descripción (snapshot) |
| codigo | VARCHAR(50) | Código del producto/servicio |
| cantidad | NUMERIC(15,2) | Cantidad |
| precio_unitario | NUMERIC(19,2) | Precio unitario |
| subtotal | NUMERIC(19,2) | Subtotal de la línea |
| tipo_iva | VARCHAR(10) | EXENTA/5/10 |
| monto_iva | NUMERIC(19,2) | Monto de IVA |
| total_linea | NUMERIC(19,2) | Total de la línea |
| numero_linea | INTEGER | Número de línea |

---

## 🔍 Índices Creados

### financiero.timbrado
- `idx_timbrado_empresa` (id_empresa)
- `idx_timbrado_activo` (activo)
- `idx_timbrado_vigencia` (fecha_inicio_vigencia, fecha_fin_vigencia)
- `uq_timbrado_numero` (numero_timbrado) UNIQUE

### financiero.factura
- `idx_factura_numero` (numero_factura)
- `idx_factura_timbrado` (timbrado)
- `idx_factura_fecha` (fecha_emision)
- `idx_factura_cliente` (id_cliente)
- `idx_factura_estado` (estado)
- `idx_factura_empresa` (id_empresa)
- `idx_factura_venta` (id_venta)
- `uq_factura_numero_timbrado` (numero_factura, timbrado) UNIQUE

### financiero.detalle_factura
- `idx_detalle_factura_factura` (id_factura)
- `idx_detalle_factura_producto` (id_producto)
- `idx_detalle_factura_servicio` (id_servicio)

---

## 🧪 Testing

### Queries GraphQL Disponibles
- ✅ `timbrado(id)` - Obtener un timbrado
- ✅ `timbrados` - Listar todos los timbrados
- ✅ `timbradosPorEmpresa(idEmpresa)` - Listar por empresa
- ✅ `timbradosActivosPorEmpresa(idEmpresa)` - Listar activos
- ✅ `timbradoDisponible(idEmpresa)` - Obtener disponible
- ✅ `factura(id)` - Obtener una factura
- ✅ `facturas(page, size)` - Listar todas (paginado)
- ✅ `facturasPorEmpresaYEstado(...)` - Listar por estado
- ✅ `facturasPorCliente(...)` - Listar por cliente
- ✅ `buscarFacturas(...)` - Buscar con filtro
- ✅ `facturasParaLibroVentas(...)` - Para reportes

### Mutations GraphQL Disponibles
- ✅ `registrarTimbrado(input)` - Crear timbrado
- ✅ `actualizarTimbrado(id, input)` - Actualizar timbrado
- ✅ `desactivarTimbrado(id)` - Desactivar
- ✅ `activarTimbrado(id)` - Activar
- ✅ `eliminarTimbrado(id)` - Eliminar
- ✅ `emitirFactura(input)` - Emitir factura
- ✅ `anularFactura(id, motivo)` - Anular factura

---

## 🚀 Próximos Pasos

### Para probar:
1. Ejecutar la aplicación: `./mvnw spring-boot:run`
2. Acceder a GraphiQL: `http://localhost:8081/graphiql`
3. Usar ejemplos de `FACTURACION_GRAPHQL_EJEMPLOS.md`

### Para integrar con frontend:
1. El módulo Angular deberá crear:
   - Servicios GraphQL
   - Componentes de UI
   - Rutas
   - Impresión de facturas

### Para facturación electrónica (futuro):
1. Crear tablas adicionales:
   - `financiero.factura_electronica`
   - `financiero.configuracion_sifen`
2. Implementar servicios de integración con SIFEN
3. Implementar generación de XML
4. Implementar firma digital
5. Implementar generación de QR

---

## ✨ Características Destacadas

1. **Numeración Segura**: Control de concurrencia para evitar duplicados
2. **Inmutabilidad Histórica**: Snapshot de datos garantiza trazabilidad
3. **Validaciones Exhaustivas**: Múltiples capas de validación
4. **Cálculos Automáticos**: IVA y totales calculados automáticamente
5. **Auditoría Completa**: Registro de todos los usuarios y fechas
6. **Flexible**: Soporta facturación con o sin cliente
7. **Extensible**: Preparado para facturación electrónica
8. **Normativo**: Cumple con requerimientos de SET Paraguay

---

## 📝 Notas Importantes

- ⚠️ El timbrado de ejemplo en la migración es ficticio y debe reemplazarse con datos reales
- ⚠️ La numeración secuencial usa `synchronized` a nivel de método, considera usar locks de base de datos para mayor seguridad en producción
- ⚠️ Los datos de empresa deben completarse antes de emitir facturas reales
- ✅ El sistema está listo para usar en desarrollo
- ✅ Todos los cálculos usan `BigDecimal` para precisión monetaria
- ✅ Las validaciones previenen la mayoría de errores comunes

---

**Estado**: ✅ Implementación Backend Completada
**Próximo paso**: Desarrollo del Frontend Angular
