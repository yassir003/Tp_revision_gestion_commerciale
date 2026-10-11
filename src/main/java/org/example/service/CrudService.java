package org.example.service;

import org.example.dao.GenericDAO;
import java.util.List;

public class CrudService<T, ID extends java.io.Serializable> {
    private final GenericDAO<T, ID> dao;

    public CrudService(Class<T> entityClass) {
        this.dao = new GenericDAO<>(entityClass);
    }

    public T getById(ID id) {
        return dao.find(id);
    }

    public List<T> getAll() {
        return dao.findAll();
    }

    public void create(T entity) {
        // Exemple de validation simple
        if (entity == null) {
            throw new IllegalArgumentException("L'entité ne doit pas être nulle");
        }
        dao.create(entity);
    }

    public T update(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entité ne doit pas être nulle");
        }
        return dao.update(entity);
    }

    public void delete(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entité ne doit pas être nulle");
        }
        dao.delete(entity);
    }
}
