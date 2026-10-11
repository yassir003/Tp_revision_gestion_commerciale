package org.example.service;

import org.example.entities.Categorie;

public class CategorieService extends CrudService<Categorie, Long> {
    public CategorieService() {
        super(Categorie.class);
    }
}
