# Módulo de Facturación - Ejemplos de Uso GraphQL

## 🔧 Timbrados

### 1. Registrar un Timbrado

```graphql
mutation {
  registrarTimbrado(input: {
    numeroTimbrado: "12345678"
    establecimiento: "001"
    puntoExpedicion: "001"
    numeroInicial: 1
    numeroFinal: 50000
    numeroActual: 1
    fechaInicioVigencia: "2026-01-01"
    fechaFinVigencia: "2026-12-31"
    idEmpresa: 1
    tipoFactura: "PAPEL"
    activo: true
  }) {
    id_timbrado
    numero_timbrado
    numeros_disponibles
    esta_vigente
    dias_hasta_vencimiento
  }
}
```

### 2. Listar Timbrados de una Empresa

```graphql
query {
  timbradosPorEmpresa(idEmpresa: 1) {
    id_timbrado
    numero_timbrado
    establecimiento
    punto_expedicion
    numero_inicial
    numero_final
    numero_actual
    numeros_disponibles
    esta_vigente
    activo
    fecha_inicio_vigencia
    fecha_fin_vigencia
  }
}
```

### 3. Obtener Timbrado Disponible

```graphql
query {
  timbradoDisponible(idEmpresa: 1) {
    id_timbrado
    numero_timbrado
    numeros_disponibles
    esta_vigente
    dias_hasta_vencimiento
  }
}
```

### 4. Desactivar un Timbrado

```graphql
mutation {
  desactivarTimbrado(id: 1) {
    id_timbrado
    activo
  }
}
```

---

## 📄 Facturas

### 1. Emitir una Factura

```graphql
mutation {
  emitirFactura(input: {
    idTimbrado: 1
    idCliente: 5
    idEmpresa: 1
    formaPago: "EFECTIVO"
    moneda: "PYG"
    observaciones: "Factura de venta de productos"
    detalles: [
      {
        idProducto: 10
        descripcion: "ACEITE MOTOR 5W30"
        cantidad: 2
        precioUnitario: 45000
        tipoIva: "10"
      },
      {
        idProducto: 15
        descripcion: "FILTRO DE ACEITE"
        cantidad: 1
        precioUnitario: 35000
        tipoIva: "10"
      },
      {
        idServicio: 3
        descripcion: "CAMBIO DE ACEITE Y FILTRO"
        cantidad: 1
        precioUnitario: 80000
        tipoIva: "10"
      }
    ]
  }) {
    id_factura
    numero_factura
    timbrado
    fecha_emision
    cliente_nombre
    cliente_ruc
    subtotal
    total_iva_5
    total_iva_10
    total_exenta
    total_iva
    total
    estado
    detalles {
      descripcion
      cantidad
      precio_unitario
      tipo_iva
      monto_iva
      total_linea
    }
  }
}
```

### 2. Emitir Factura Sin Cliente (Consumidor Final)

```graphql
mutation {
  emitirFactura(input: {
    idTimbrado: 1
    idEmpresa: 1
    clienteNombre: "CONSUMIDOR FINAL"
    clienteDocumento: "0"
    formaPago: "EFECTIVO"
    moneda: "PYG"
    detalles: [
      {
        descripcion: "PRODUCTO VARIOS"
        cantidad: 1
        precioUnitario: 50000
        tipoIva: "10"
      }
    ]
  }) {
    id_factura
    numero_factura
    cliente_nombre
    total
  }
}
```

### 3. Consultar una Factura

```graphql
query {
  factura(id: 1) {
    id_factura
    numero_factura
    timbrado
    fecha_emision
    cliente {
      id_cliente
      ruc
      persona {
        nombre
        apellido
        documento
      }
    }
    cliente_nombre
    cliente_ruc
    subtotal
    total_iva_5
    total_iva_10
    total_exenta
    total_iva
    total
    forma_pago
    moneda
    estado
    observaciones
    detalles {
      numero_linea
      descripcion
      codigo
      cantidad
      precio_unitario
      subtotal
      tipo_iva
      monto_iva
      total_linea
    }
  }
}
```

### 4. Buscar Facturas con Filtro

```graphql
query {
  buscarFacturas(
    idEmpresa: 1
    filtro: "Juan"
    page: 0
    size: 10
  ) {
    content {
      id_factura
      numero_factura
      fecha_emision
      cliente_nombre
      cliente_ruc
      total
      estado
    }
    pageInfo {
      totalElements
      totalPages
      currentPage
      pageSize
      hasNext
      hasPrevious
    }
  }
}
```

### 5. Listar Facturas por Estado

```graphql
query {
  facturasPorEmpresaYEstado(
    idEmpresa: 1
    estado: "EMITIDA"
    page: 0
    size: 20
  ) {
    content {
      id_factura
      numero_factura
      fecha_emision
      cliente_nombre
      total
      estado
    }
    pageInfo {
      totalElements
      totalPages
    }
  }
}
```

### 6. Anular una Factura

```graphql
mutation {
  anularFactura(
    id: 1
    motivo: "ERROR EN DATOS DEL CLIENTE, SE EMITE NUEVA FACTURA"
  ) {
    id_factura
    numero_factura
    estado
    fecha_anulacion
    motivo_anulacion
  }
}
```

### 7. Obtener Facturas para Libro de Ventas

```graphql
query {
  facturasParaLibroVentas(
    idEmpresa: 1
    fechaInicio: "2026-09-01T00:00:00"
    fechaFin: "2026-09-30T23:59:59"
  ) {
    numero_factura
    fecha_emision
    cliente_nombre
    cliente_ruc
    subtotal
    total_iva_5
    total_iva_10
    total_exenta
    total
    estado
  }
}
```

### 8. Facturas de un Cliente Específico

```graphql
query {
  facturasPorCliente(
    idCliente: 5
    page: 0
    size: 10
  ) {
    content {
      id_factura
      numero_factura
      fecha_emision
      total
      estado
      forma_pago
      detalles {
        descripcion
        cantidad
        precio_unitario
        total_linea
      }
    }
    pageInfo {
      totalElements
    }
  }
}
```

---

## 💡 Tipos de IVA en Paraguay

- **"EXENTA"**: Sin IVA
- **"5"**: IVA del 5% (productos de primera necesidad)
- **"10"**: IVA del 10% (IVA estándar)

---

## 📋 Estados de Factura

- **"EMITIDA"**: Factura emitida y válida
- **"ANULADA"**: Factura anulada (no se puede revertir)

---

## 💵 Formas de Pago

- **"EFECTIVO"**
- **"TARJETA"**
- **"CHEQUE"**
- **"TRANSFERENCIA"**
- **"QR"**
- **"CREDITO"**

---

## 🌍 Monedas Soportadas

- **"PYG"**: Guaraníes (default)
- **"USD"**: Dólares
- **"BRL"**: Reales
- **"EUR"**: Euros
- **"ARS"**: Pesos Argentinos

---

## ⚠️ Validaciones Importantes

1. **Numeración Secuencial**: El sistema garantiza numeración estrictamente secuencial
2. **Timbrado Vigente**: Solo se puede facturar con timbrado activo y vigente
3. **Números Disponibles**: El sistema verifica que haya números disponibles antes de emitir
4. **Anulación Irreversible**: Una factura anulada no se puede reactivar
5. **Snapshot de Datos**: Los datos del cliente se guardan en la factura (no se actualizan si el cliente cambia)

---

## 🔐 Seguridad

- Todas las operaciones requieren autenticación
- El usuario emisor se registra automáticamente
- El usuario que anula la factura se registra automáticamente
- Las fechas se registran automáticamente

---

## 📊 Cálculo de IVA

El sistema calcula el IVA automáticamente según el tipo:

```
Para IVA 5%:
monto_iva = subtotal × 0.05 / 1.05

Para IVA 10%:
monto_iva = subtotal × 0.10 / 1.10

Para EXENTA:
monto_iva = 0
```

---

## 🚀 Testing en GraphQL Playground

Puedes probar todas estas operaciones en:
`http://localhost:8081/graphiql`

Recuerda incluir el token de autenticación en los headers:

```json
{
  "Authorization": "Bearer YOUR_JWT_TOKEN"
}
```
