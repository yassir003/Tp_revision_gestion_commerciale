package org.example.service;

import org.example.entities.Commande;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class CommandeService extends CrudService<Commande, Long> {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("defaultPU");

    public CommandeService() {
        super(Commande.class);
    }

    @Override
    public void create(Commande cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("La commande ne doit pas être nulle");
        }
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // em.merge permet de réattacher les entités Produit détachées présentes dans le graphe
            Commande merged = em.merge(cmd);
            cmd.setId(merged.getId());
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
