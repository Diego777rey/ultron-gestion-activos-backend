-- Permisos de compras, proveedores y vendedores.
-- El menú los oculta si el rol no tiene la acción VER.

INSERT INTO personas.permiso (modulo, accion, descripcion)
SELECT v.modulo, v.accion, v.descripcion
FROM (VALUES
    ('COMPRAS', 'VER', 'Ver compras'),
    ('COMPRAS', 'CREAR', 'Crear compras'),
    ('COMPRAS', 'EDITAR', 'Editar compras'),
    ('COMPRAS', 'ELIMINAR', 'Eliminar compras'),
    ('PROVEEDORES', 'VER', 'Ver proveedores'),
    ('PROVEEDORES', 'CREAR', 'Crear proveedores'),
    ('PROVEEDORES', 'EDITAR', 'Editar proveedores'),
    ('PROVEEDORES', 'ELIMINAR', 'Eliminar proveedores'),
    ('VENDEDORES', 'VER', 'Ver vendedores'),
    ('VENDEDORES', 'CREAR', 'Crear vendedores'),
    ('VENDEDORES', 'EDITAR', 'Editar vendedores'),
    ('VENDEDORES', 'ELIMINAR', 'Eliminar vendedores')
) AS v (modulo, accion, descripcion)
WHERE NOT EXISTS (
    SELECT 1 FROM personas.permiso p WHERE p.modulo = v.modulo AND p.accion = v.accion
);

INSERT INTO personas.role_permiso (role_id, permiso_id)
SELECT r.id, p.id
FROM personas.role r
JOIN personas.permiso p ON p.modulo IN ('COMPRAS', 'PROVEEDORES', 'VENDEDORES')
WHERE r.descripcion = 'Admin'
  AND NOT EXISTS (
      SELECT 1 FROM personas.role_permiso x
      WHERE x.role_id = r.id AND x.permiso_id = p.id
  );
