package org.example.service;

import org.example.dto.VenteCategorieDTO;
import org.example.entities.Categorie;
import org.example.entities.Commande;
import org.example.entities.CommandeProduit;
import org.example.entities.Produit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class RapportVentesService {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("defaultPU");

    public List<VenteCategorieDTO> getRapportVentesMoisPrecedent() {
        YearMonth moisPrecedent = YearMonth.now().minusMonths(1);
        LocalDate debut = moisPrecedent.atDay(1);
        LocalDate fin = moisPrecedent.atEndOfMonth();
        return getRapportVentesPeriode(debut, fin);
    }

    public List<VenteCategorieDTO> getRapportVentesPeriode(LocalDate dateDebut, LocalDate dateFin) {
        EntityManager em = emf.createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<VenteCategorieDTO> cq = cb.createQuery(VenteCategorieDTO.class);
            Root<CommandeProduit> root = cq.from(CommandeProduit.class);

            // Jointures
            Join<CommandeProduit, Produit> produitJoin = root.join("produit");
            Join<Produit, Categorie> categorieJoin = produitJoin.join("categorie");
            Join<CommandeProduit, Commande> commandeJoin = root.join("commande");

            // Calcul du chiffre d'affaires : sum(prix * quantite)
            // Utilisation de CriteriaBuilder.prod et CriteriaBuilder.sum
            Expression<BigDecimal> prix = produitJoin.get("prix");
            Expression<BigDecimal> quantite = root.get("quantite").as(BigDecimal.class);
            Expression<BigDecimal> montantLigne = cb.prod(prix, quantite);
            Expression<BigDecimal> totalCA = cb.sum(montantLigne);

            // Construction du DTO
            cq.select(cb.construct(VenteCategorieDTO.class, categorieJoin.get("nom"), totalCA));

            // Filtrage sur la période de la commande
            if (dateDebut != null && dateFin != null) {
                cq.where(cb.between(commandeJoin.get("dateCommande"), dateDebut, dateFin));
            } else if (dateDebut != null) {
                cq.where(cb.greaterThanOrEqualTo(commandeJoin.get("dateCommande"), dateDebut));
            } else if (dateFin != null) {
                cq.where(cb.lessThanOrEqualTo(commandeJoin.get("dateCommande"), dateFin));
            }

            // Regroupement par catégorie
            cq.groupBy(categorieJoin.get("id"), categorieJoin.get("nom"));
            cq.orderBy(cb.desc(totalCA));

            return em.createQuery(cq).getResultList();
        } finally {
            em.close();
        }
    }
}
