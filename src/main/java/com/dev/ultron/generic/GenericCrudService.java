package com.dev.ultron.generic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio CRUD genérico reutilizable para todas las entidades del sistema.
 * Proporciona operaciones de Alta, Baja, Modificación y Consulta (ABMS).
 *
 * @param <T>  Tipo de la entidad JPA
 * @param <ID> Tipo del identificador de la entidad
 */
public abstract class GenericCrudService<T, ID> {

    /**
     * Retorna el repositorio JPA específico de la entidad.
     * Cada servicio concreto debe implementar este método.
     */
    protected abstract JpaRepository<T, ID> getRepository();

    /**
     * Hook opcional para validar la entidad antes de guardar.
     * Los servicios concretos pueden sobreescribir este método.
     */
    protected void validarAntesDeGuardar(T entity) {
        // Hook por defecto: sin validación adicional
    }

    /**
     * Hook opcional para validar antes de eliminar.
     */
    protected void validarAntesDeEliminar(ID id) {
        // Hook por defecto: sin validación adicional
    }

    /**
     * Hook opcional que se ejecuta después de guardar.
     */
    protected void despuesDeGuardar(T entity) {
        // Hook por defecto: sin acción post-guardado
    }

    // ==================== CONSULTAS ====================

    /**
     * Lista todas las entidades.
     */
    @Transactional(readOnly = true)
    public List<T> listarTodos() {
        return getRepository().findAll();
    }

    /**
     * Busca una entidad por su ID.
     */
    @Transactional(readOnly = true)
    public Optional<T> buscarPorId(ID id) {
        return getRepository().findById(id);
    }

    /**
     * Busca una entidad por ID y lanza excepción si no existe.
     */
    @Transactional(readOnly = true)
    public T buscarPorIdOrThrow(ID id) {
        return getRepository().findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Registro no encontrado con ID: " + id));
    }

    /**
     * Verifica si existe una entidad con el ID dado.
     */
    @Transactional(readOnly = true)
    public boolean existePorId(ID id) {
        return getRepository().existsById(id);
    }

    /**
     * Cuenta el total de registros.
     */
    @Transactional(readOnly = true)
    public long contarTodos() {
        return getRepository().count();
    }

    // ==================== ALTA (GUARDAR) ====================

    /**
     * Guarda una nueva entidad.
     */
    @Transactional
    public T guardar(T entity) {
        validarAntesDeGuardar(entity);
        T saved = getRepository().save(entity);
        despuesDeGuardar(saved);
        return saved;
    }

    /**
     * Guarda una lista de entidades.
     */
    @Transactional
    public List<T> guardarTodos(List<T> entities) {
        entities.forEach(this::validarAntesDeGuardar);
        List<T> savedList = getRepository().saveAll(entities);
        savedList.forEach(this::despuesDeGuardar);
        return savedList;
    }

    // ==================== MODIFICACIÓN ====================

    /**
     * Actualiza una entidad existente.
     * La entidad debe tener su ID establecido.
     */
    @Transactional
    public T actualizar(T entity) {
        validarAntesDeGuardar(entity);
        T updated = getRepository().save(entity);
        despuesDeGuardar(updated);
        return updated;
    }

    // ==================== BAJA (ELIMINAR) ====================

    /**
     * Elimina una entidad por su ID.
     */
    @Transactional
    public void eliminarPorId(ID id) {
        validarAntesDeEliminar(id);
        if (!getRepository().existsById(id)) {
            throw new EntityNotFoundException(
                    "No se puede eliminar. Registro no encontrado con ID: " + id);
        }
        getRepository().deleteById(id);
    }

    /**
     * Elimina una entidad.
     */
    @Transactional
    public void eliminar(T entity) {
        getRepository().delete(entity);
    }
}
