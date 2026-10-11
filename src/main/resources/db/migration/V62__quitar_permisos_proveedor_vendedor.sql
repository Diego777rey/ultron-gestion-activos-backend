-- Proveedores y vendedores se habilitan con el permiso COMPRAS.
-- Admin ya ve todo porque tiene ese permiso. No hacen falta módulos aparte.

DELETE FROM personas.role_permiso rp
USING personas.permiso p
WHERE rp.permiso_id = p.id
  AND p.modulo IN ('PROVEEDORES', 'VENDEDORES');

DELETE FROM personas.permiso
WHERE modulo IN ('PROVEEDORES', 'VENDEDORES');
