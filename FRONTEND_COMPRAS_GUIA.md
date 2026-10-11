# Guía de Implementación Frontend - Módulo de Compras

Esta guía describe cómo implementar el frontend para el módulo de compras (Proveedor y Vendedor) en Angular, siguiendo los patrones establecidos en el sistema.

## 📋 Estructura a Crear

```
src/app/
├── pages/
│   └── compras/
│       ├── proveedor/
│       │   ├── proveedor-list/
│       │   │   ├── proveedor-list.component.ts
│       │   │   ├── proveedor-list.component.html
│       │   │   └── proveedor-list.component.scss
│       │   └── proveedor-form/
│       │       ├── proveedor-form.component.ts
│       │       ├── proveedor-form.component.html
│       │       └── proveedor-form.component.scss
│       └── vendedor/
│           ├── vendedor-list/
│           │   ├── vendedor-list.component.ts
│           │   ├── vendedor-list.component.html
│           │   └── vendedor-list.component.scss
│           └── vendedor-form/
│               ├── vendedor-form.component.ts
│               ├── vendedor-form.component.html
│               └── vendedor-form.component.scss
└── shared/
    ├── models/
    │   └── compras.model.ts
    └── services/
        ├── proveedor.service.ts
        └── vendedor.service.ts
```

## 🎯 Paso 1: Modelos TypeScript

Crear archivo `src/app/shared/models/compras.model.ts`:

```typescript
// ========== PROVEEDOR ==========

export enum TipoPersona {
  FISICA = 'FISICA',
  JURIDICA = 'JURIDICA'
}

export interface ProveedorOutput {
  id_proveedor: number;
  tipo_persona: TipoPersona;
  nombre?: string;
  apellido?: string;
  razon_social?: string;
  nombre_comercial?: string;
  nombre_fantasia?: string;
  ruc: string;
  documento?: string;
  direccion?: string;
  telefono?: string;
  email?: string;
  ciudad?: string;
  pais?: string;
  codigo_postal?: string;
  sitio_web?: string;
  observaciones?: string;
  activo: boolean;
  fecha_registro?: string;
  fecha_ultima_modificacion?: string;
  nombre_completo?: string;
}

export interface ProveedorInput {
  tipoPersona: TipoPersona;
  nombre?: string;
  apellido?: string;
  razonSocial?: string;
  nombreComercial?: string;
  nombreFantasia?: string;
  ruc: string;
  documento?: string;
  direccion?: string;
  telefono?: string;
  email?: string;
  ciudad?: string;
  pais?: string;
  codigoPostal?: string;
  sitioWeb?: string;
  observaciones?: string;
  activo?: boolean;
}

// ========== VENDEDOR ==========

export interface VendedorOutput {
  id_vendedor: number;
  id_proveedor: number;
  proveedor_nombre?: string;
  nombre: string;
  apellido: string;
  telefono?: string;
  email?: string;
  celular?: string;
  cargo?: string;
  observaciones?: string;
  activo: boolean;
  fecha_registro?: string;
  fecha_ultima_modificacion?: string;
  nombre_completo?: string;
}

export interface VendedorInput {
  idProveedor: number;
  nombre: string;
  apellido: string;
  telefono?: string;
  email?: string;
  celular?: string;
  cargo?: string;
  observaciones?: string;
  activo?: boolean;
}
```

## 🔌 Paso 2: Servicios GraphQL

### Servicio de Proveedor

Crear archivo `src/app/shared/services/proveedor.service.ts`:

```typescript
import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import { Observable, map } from 'rxjs';
import { ProveedorOutput, ProveedorInput } from '../models/compras.model';

const PROVEEDOR_FIELDS = `
  id_proveedor
  tipo_persona
  nombre
  apellido
  razon_social
  nombre_comercial
  nombre_fantasia
  ruc
  documento
  direccion
  telefono
  email
  ciudad
  pais
  codigo_postal
  sitio_web
  observaciones
  activo
  fecha_registro
  fecha_ultima_modificacion
  nombre_completo
`;

@Injectable({
  providedIn: 'root'
})
export class ProveedorService {

  constructor(private apollo: Apollo) { }

  // Listar todos los proveedores
  listarProveedores(): Observable<ProveedorOutput[]> {
    return this.apollo.query<{ proveedores: ProveedorOutput[] }>({
      query: gql\`
        query {
          proveedores {
            \${PROVEEDOR_FIELDS}
          }
        }
      \`,
      fetchPolicy: 'network-only'
    }).pipe(map(result => result.data.proveedores));
  }

  // Listar solo proveedores activos
  listarProveedoresActivos(): Observable<ProveedorOutput[]> {
    return this.apollo.query<{ proveedoresActivos: ProveedorOutput[] }>({
      query: gql\`
        query {
          proveedoresActivos {
            \${PROVEEDOR_FIELDS}
          }
        }
      \`,
      fetchPolicy: 'network-only'
    }).pipe(map(result => result.data.proveedoresActivos));
  }

  // Obtener un proveedor por ID
  obtenerProveedor(id: number): Observable<ProveedorOutput> {
    return this.apollo.query<{ proveedor: ProveedorOutput }>({
      query: gql\`
        query($id: ID!) {
          proveedor(id: $id) {
            \${PROVEEDOR_FIELDS}
          }
        }
      \`,
      variables: { id },
      fetchPolicy: 'network-only'
    }).pipe(map(result => result.data.proveedor));
  }

  // Buscar por RUC
  buscarPorRuc(ruc: string): Observable<ProveedorOutput> {
    return this.apollo.query<{ proveedorPorRuc: ProveedorOutput }>({
      query: gql\`
        query($ruc: String!) {
          proveedorPorRuc(ruc: $ruc) {
            \${PROVEEDOR_FIELDS}
          }
        }
      \`,
      variables: { ruc },
      fetchPolicy: 'network-only'
    }).pipe(map(result => result.data.proveedorPorRuc));
  }

  // Registrar nuevo proveedor
  registrarProveedor(input: ProveedorInput): Observable<ProveedorOutput> {
    return this.apollo.mutate<{ registrarProveedor: ProveedorOutput }>({
      mutation: gql\`
        mutation($input: ProveedorInput!) {
          registrarProveedor(input: $input) {
            \${PROVEEDOR_FIELDS}
          }
        }
      \`,
      variables: { input },
      refetchQueries: ['proveedores', 'proveedoresActivos']
    }).pipe(map(result => result.data!.registrarProveedor));
  }

  // Actualizar proveedor
  actualizarProveedor(id: number, input: ProveedorInput): Observable<ProveedorOutput> {
    return this.apollo.mutate<{ actualizarProveedor: ProveedorOutput }>({
      mutation: gql\`
        mutation($id: ID!, $input: ProveedorInput!) {
          actualizarProveedor(id: $id, input: $input) {
            \${PROVEEDOR_FIELDS}
          }
        }
      \`,
      variables: { id, input },
      refetchQueries: ['proveedores', 'proveedoresActivos']
    }).pipe(map(result => result.data!.actualizarProveedor));
  }

  // Eliminar proveedor
  eliminarProveedor(id: number): Observable<boolean> {
    return this.apollo.mutate<{ eliminarProveedor: boolean }>({
      mutation: gql\`
        mutation($id: ID!) {
          eliminarProveedor(id: $id)
        }
      \`,
      variables: { id },
      refetchQueries: ['proveedores', 'proveedoresActivos']
    }).pipe(map(result => result.data!.eliminarProveedor));
  }
}
```

### Servicio de Vendedor

Crear archivo `src/app/shared/services/vendedor.service.ts`:

```typescript
import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import { Observable, map } from 'rxjs';
import { VendedorOutput, VendedorInput } from '../models/compras.model';

const VENDEDOR_FIELDS = `
  id_vendedor
  id_proveedor
  proveedor_nombre
  nombre
  apellido
  telefono
  email
  celular
  cargo
  observaciones
  activo
  fecha_registro
  fecha_ultima_modificacion
  nombre_completo
`;

@Injectable({
  providedIn: 'root'
})
export class VendedorService {

  constructor(private apollo: Apollo) { }

  // Listar todos los vendedores
  listarVendedores(): Observable<VendedorOutput[]> {
    return this.apollo.query<{ vendedores: VendedorOutput[] }>({
      query: gql\`
        query {
          vendedores {
            \${VENDEDOR_FIELDS}
          }
        }
      \`,
      fetchPolicy: 'network-only'
    }).pipe(map(result => result.data.vendedores));
  }

  // Listar solo vendedores activos
  listarVendedoresActivos(): Observable<VendedorOutput[]> {
    return this.apollo.query<{ vendedoresActivos: VendedorOutput[] }>({
      query: gql\`
        query {
          vendedoresActivos {
            \${VENDEDOR_FIELDS}
          }
        }
      \`,
      fetchPolicy: 'network-only'
    }).pipe(map(result => result.data.vendedoresActivos));
  }

  // Listar vendedores por proveedor
  listarVendedoresPorProveedor(idProveedor: number): Observable<VendedorOutput[]> {
    return this.apollo.query<{ vendedoresPorProveedor: VendedorOutput[] }>({
      query: gql\`
        query($idProveedor: ID!) {
          vendedoresPorProveedor(idProveedor: $idProveedor) {
            \${VENDEDOR_FIELDS}
          }
        }
      \`,
      variables: { idProveedor },
      fetchPolicy: 'network-only'
    }).pipe(map(result => result.data.vendedoresPorProveedor));
  }

  // Obtener un vendedor por ID
  obtenerVendedor(id: number): Observable<VendedorOutput> {
    return this.apollo.query<{ vendedor: VendedorOutput }>({
      query: gql\`
        query($id: ID!) {
          vendedor(id: $id) {
            \${VENDEDOR_FIELDS}
          }
        }
      \`,
      variables: { id },
      fetchPolicy: 'network-only'
    }).pipe(map(result => result.data.vendedor));
  }

  // Registrar nuevo vendedor
  registrarVendedor(input: VendedorInput): Observable<VendedorOutput> {
    return this.apollo.mutate<{ registrarVendedor: VendedorOutput }>({
      mutation: gql\`
        mutation($input: VendedorInput!) {
          registrarVendedor(input: $input) {
            \${VENDEDOR_FIELDS}
          }
        }
      \`,
      variables: { input },
      refetchQueries: ['vendedores', 'vendedoresActivos']
    }).pipe(map(result => result.data!.registrarVendedor));
  }

  // Actualizar vendedor
  actualizarVendedor(id: number, input: VendedorInput): Observable<VendedorOutput> {
    return this.apollo.mutate<{ actualizarVendedor: VendedorOutput }>({
      mutation: gql\`
        mutation($id: ID!, $input: VendedorInput!) {
          actualizarVendedor(id: $id, input: $input) {
            \${VENDEDOR_FIELDS}
          }
        }
      \`,
      variables: { id, input },
      refetchQueries: ['vendedores', 'vendedoresActivos']
    }).pipe(map(result => result.data!.actualizarVendedor));
  }

  // Eliminar vendedor
  eliminarVendedor(id: number): Observable<boolean> {
    return this.apollo.mutate<{ eliminarVendedor: boolean }>({
      mutation: gql\`
        mutation($id: ID!) {
          eliminarVendedor(id: $id)
        }
      \`,
      variables: { id },
      refetchQueries: ['vendedores', 'vendedoresActivos']
    }).pipe(map(result => result.data!.eliminarVendedor));
  }
}
```

## 📝 Paso 3: Componente de Listado de Proveedores

### TypeScript - `proveedor-list.component.ts`

```typescript
import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ProveedorService } from '../../../shared/services/proveedor.service';
import { ProveedorOutput, TipoPersona } from '../../../shared/models/compras.model';

@Component({
  selector: 'app-proveedor-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './proveedor-list.component.html',
  styleUrls: ['./proveedor-list.component.scss']
})
export class ProveedorListComponent implements OnInit {
  protected readonly proveedores = signal<ProveedorOutput[]>([]);
  protected readonly cargando = signal<boolean>(false);
  protected readonly error = signal<string | null>(null);

  constructor(
    private proveedorService: ProveedorService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.cargarProveedores();
  }

  protected cargarProveedores(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.proveedorService.listarProveedores().subscribe({
      next: (data) => {
        this.proveedores.set(data);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set('Error al cargar proveedores');
        this.cargando.set(false);
        console.error(err);
      }
    });
  }

  protected nuevoProveedor(): void {
    this.router.navigate(['/compras/proveedor/nuevo']);
  }

  protected editarProveedor(id: number): void {
    this.router.navigate(['/compras/proveedor/editar', id]);
  }

  protected verVendedores(id: number): void {
    this.router.navigate(['/compras/vendedor'], { queryParams: { proveedor: id } });
  }

  protected eliminarProveedor(proveedor: ProveedorOutput): void {
    if (!confirm(\`¿Está seguro de eliminar el proveedor \${proveedor.nombre_completo}?\`)) {
      return;
    }

    this.proveedorService.eliminarProveedor(proveedor.id_proveedor).subscribe({
      next: () => {
        this.cargarProveedores();
      },
      error: (err) => {
        alert('Error al eliminar proveedor');
        console.error(err);
      }
    });
  }

  protected getTipoBadgeClass(tipo: TipoPersona): string {
    return tipo === TipoPersona.FISICA ? 'badge-info' : 'badge-success';
  }
}
```

### HTML - `proveedor-list.component.html`

```html
<div class="page-container">
  <header class="page-header">
    <div class="page-header__content">
      <div class="page-header__title">
        <span class="material-icons">business</span>
        <h1>Proveedores</h1>
      </div>
      <button class="btn btn-primary" (click)="nuevoProveedor()">
        <span class="material-icons">add</span>
        Nuevo Proveedor
      </button>
    </div>
  </header>

  <div class="page-content">
    @if (cargando()) {
      <div class="loading">
        <span class="material-icons spin">refresh</span>
        <p>Cargando proveedores...</p>
      </div>
    } @else if (error()) {
      <div class="error-message">
        <span class="material-icons">error</span>
        <p>{{ error() }}</p>
        <button class="btn btn-secondary" (click)="cargarProveedores()">Reintentar</button>
      </div>
    } @else {
      <div class="table-container">
        <table class="data-table">
          <thead>
            <tr>
              <th>Tipo</th>
              <th>Nombre/Razón Social</th>
              <th>RUC</th>
              <th>Teléfono</th>
              <th>Email</th>
              <th>Ciudad</th>
              <th>Estado</th>
              <th class="actions-column">Acciones</th>
            </tr>
          </thead>
          <tbody>
            @for (proveedor of proveedores(); track proveedor.id_proveedor) {
              <tr [class.inactive]="!proveedor.activo">
                <td>
                  <span class="badge" [class]="getTipoBadgeClass(proveedor.tipo_persona)">
                    {{ proveedor.tipo_persona === 'FISICA' ? 'Física' : 'Jurídica' }}
                  </span>
                </td>
                <td>{{ proveedor.nombre_completo }}</td>
                <td>{{ proveedor.ruc }}</td>
                <td>{{ proveedor.telefono || '-' }}</td>
                <td>{{ proveedor.email || '-' }}</td>
                <td>{{ proveedor.ciudad || '-' }}</td>
                <td>
                  <span class="badge" [class.badge-success]="proveedor.activo" [class.badge-danger]="!proveedor.activo">
                    {{ proveedor.activo ? 'Activo' : 'Inactivo' }}
                  </span>
                </td>
                <td class="actions-column">
                  <button class="btn-icon" (click)="verVendedores(proveedor.id_proveedor)" title="Ver vendedores">
                    <span class="material-icons">people</span>
                  </button>
                  <button class="btn-icon" (click)="editarProveedor(proveedor.id_proveedor)" title="Editar">
                    <span class="material-icons">edit</span>
                  </button>
                  <button class="btn-icon btn-danger" (click)="eliminarProveedor(proveedor)" title="Eliminar">
                    <span class="material-icons">delete</span>
                  </button>
                </td>
              </tr>
            } @empty {
              <tr>
                <td colspan="8" class="no-data">No hay proveedores registrados</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
  </div>
</div>
```

## 📝 Paso 4: Componente de Formulario de Proveedor

### TypeScript - `proveedor-form.component.ts`

```typescript
import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProveedorService } from '../../../shared/services/proveedor.service';
import { ProveedorInput, TipoPersona } from '../../../shared/models/compras.model';

@Component({
  selector: 'app-proveedor-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './proveedor-form.component.html',
  styleUrls: ['./proveedor-form.component.scss']
})
export class ProveedorFormComponent implements OnInit {
  protected form!: FormGroup;
  protected readonly guardando = signal<boolean>(false);
  protected readonly idProveedor = signal<number | null>(null);
  protected readonly esEdicion = signal<boolean>(false);

  protected readonly TipoPersona = TipoPersona;

  constructor(
    private fb: FormBuilder,
    private proveedorService: ProveedorService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.crearFormulario();
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.idProveedor.set(+id);
      this.esEdicion.set(true);
      this.cargarProveedor(+id);
    }
  }

  private crearFormulario(): void {
    this.form = this.fb.group({
      tipoPersona: [TipoPersona.JURIDICA, Validators.required],
      // Persona Física
      nombre: [''],
      apellido: [''],
      // Persona Jurídica
      razonSocial: [''],
      nombreComercial: [''],
      nombreFantasia: [''],
      // Comunes
      ruc: ['', Validators.required],
      documento: [''],
      direccion: [''],
      telefono: [''],
      email: ['', Validators.email],
      ciudad: [''],
      pais: [''],
      codigoPostal: [''],
      sitioWeb: [''],
      observaciones: [''],
      activo: [true]
    });

    this.form.get('tipoPersona')?.valueChanges.subscribe(() => {
      this.actualizarValidadores();
    });

    this.actualizarValidadores();
  }

  private actualizarValidadores(): void {
    const tipoPersona = this.form.get('tipoPersona')?.value;

    // Limpiar validadores
    this.form.get('nombre')?.clearValidators();
    this.form.get('apellido')?.clearValidators();
    this.form.get('razonSocial')?.clearValidators();

    if (tipoPersona === TipoPersona.FISICA) {
      this.form.get('nombre')?.setValidators([Validators.required]);
      this.form.get('apellido')?.setValidators([Validators.required]);
    } else {
      this.form.get('razonSocial')?.setValidators([Validators.required]);
    }

    this.form.get('nombre')?.updateValueAndValidity();
    this.form.get('apellido')?.updateValueAndValidity();
    this.form.get('razonSocial')?.updateValueAndValidity();
  }

  private cargarProveedor(id: number): void {
    this.proveedorService.obtenerProveedor(id).subscribe({
      next: (proveedor) => {
        this.form.patchValue({
          tipoPersona: proveedor.tipo_persona,
          nombre: proveedor.nombre,
          apellido: proveedor.apellido,
          razonSocial: proveedor.razon_social,
          nombreComercial: proveedor.nombre_comercial,
          nombreFantasia: proveedor.nombre_fantasia,
          ruc: proveedor.ruc,
          documento: proveedor.documento,
          direccion: proveedor.direccion,
          telefono: proveedor.telefono,
          email: proveedor.email,
          ciudad: proveedor.ciudad,
          pais: proveedor.pais,
          codigoPostal: proveedor.codigo_postal,
          sitioWeb: proveedor.sitio_web,
          observaciones: proveedor.observaciones,
          activo: proveedor.activo
        });
      },
      error: (err) => {
        console.error(err);
        alert('Error al cargar proveedor');
        this.volver();
      }
    });
  }

  protected guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    const input: ProveedorInput = this.form.value;

    const operacion = this.esEdicion()
      ? this.proveedorService.actualizarProveedor(this.idProveedor()!, input)
      : this.proveedorService.registrarProveedor(input);

    operacion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.volver();
      },
      error: (err) => {
        this.guardando.set(false);
        console.error(err);
        alert('Error al guardar proveedor');
      }
    });
  }

  protected volver(): void {
    this.router.navigate(['/compras/proveedor']);
  }

  protected esFisica(): boolean {
    return this.form.get('tipoPersona')?.value === TipoPersona.FISICA;
  }
}
```

## 🎨 Paso 5: Rutas

Agregar en `app.routes.ts` o el archivo de rutas correspondiente:

```typescript
{
  path: 'compras',
  children: [
    {
      path: 'proveedor',
      children: [
        { path: '', component: ProveedorListComponent },
        { path: 'nuevo', component: ProveedorFormComponent },
        { path: 'editar/:id', component: ProveedorFormComponent }
      ]
    },
    {
      path: 'vendedor',
      children: [
        { path: '', component: VendedorListComponent },
        { path: 'nuevo', component: VendedorFormComponent },
        { path: 'editar/:id', component: VendedorFormComponent }
      ]
    }
  ]
}
```

## 📋 Notas Importantes

1. **Tipo de Persona**: El formulario debe mostrar/ocultar campos según el tipo de persona seleccionado
2. **Validaciones**: 
   - Persona Física: Nombre y Apellido son obligatorios
   - Persona Jurídica: Razón Social es obligatoria
   - RUC siempre es obligatorio
3. **Vendedores**: Se listan por proveedor y requieren al menos un teléfono o celular
4. **Estilos**: Seguir los patrones de diseño existentes en el sistema

## 🔄 Integración con el Sistema de Compras

Cuando implementes el módulo completo de compras, podrás:

1. Seleccionar un proveedor desde un dropdown en el formulario de compra
2. Seleccionar un vendedor asociado a ese proveedor
3. Autocompletar datos del proveedor al seleccionarlo
4. Validar que el proveedor esté activo antes de permitir la compra

## 📦 Ejemplo de Uso en Compra

```typescript
// En el servicio de compras
listarProveedoresParaCompra(): Observable<ProveedorOutput[]> {
  return this.proveedorService.listarProveedoresActivos();
}

listarVendedoresDeProveedor(idProveedor: number): Observable<VendedorOutput[]> {
  return this.vendedorService.listarVendedoresPorProveedor(idProveedor);
}
```

---

**Implementado por**: Cursor AI Agent  
**Fecha**: Octubre 2026  
**Backend**: ✅ Completo  
**Frontend**: 📝 Pendiente de implementación
