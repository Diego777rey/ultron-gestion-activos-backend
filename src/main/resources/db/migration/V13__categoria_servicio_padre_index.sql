-- Indice de apoyo para la jerarquia de categorias de servicio (subcategorias).
-- La columna id_categoria_padre ya existe desde V10.
CREATE INDEX IF NOT EXISTS ix_categoria_servicio_padre
    ON inventario.categoria_servicio (id_categoria_padre);
