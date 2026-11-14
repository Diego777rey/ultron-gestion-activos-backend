-- Categorías base de la tienda (solo se insertan si no existen por nombre).
-- El catálogo real sigue dependiendo de productos con mostrar_en_web.
INSERT INTO inventario.categoria_producto (nombre, descripcion, estado)
SELECT v.nombre, v.descripcion, TRUE
FROM (VALUES
    ('HERRAMIENTAS', 'Herramientas manuales y eléctricas para taller'),
    ('TALLER', 'Insumos y equipos de taller mecánico'),
    ('FERRETERIA', 'Ferretería general y fijaciones'),
    ('AUTOS', 'Repuestos y accesorios para automóviles'),
    ('MOTOS', 'Repuestos y accesorios para motocicletas'),
    ('BEBIDAS', 'Bebidas para el local y el mostrador'),
    ('LUBRICANTES', 'Aceites, grasas y fluidos'),
    ('ELECTRICIDAD AUTOMOTRIZ', 'Cables, sensores, fusibles y diagramas'),
    ('LIMPIEZA', 'Productos de limpieza y cuidado')
) AS v(nombre, descripcion)
WHERE NOT EXISTS (
    SELECT 1
    FROM inventario.categoria_producto c
    WHERE UPPER(TRIM(c.nombre)) = v.nombre
      AND c.id_categoria_padre IS NULL
);
