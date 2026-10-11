package org.example;

import org.example.dao.GenericDAO;
import org.example.dto.VenteCategorieDTO;
import org.example.entities.*;
import org.example.service.CommandeService;
import org.example.service.CrudService;
import org.example.service.ProduitService;
import org.example.service.RapportVentesService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("   DEMONSTRATION & TESTS DE REVISION JEE / JPA / HIBERNATE");
        System.out.println("=========================================================\n");

        try {
            // Initialisation des services et DAOs
            CrudService<Categorie, Long> catService = new CrudService<>(Categorie.class);
            ProduitService produitService = new ProduitService();
            CommandeService commandeService = new CommandeService();
            RapportVentesService rapportService = new RapportVentesService();
            GenericDAO<Client, Long> clientDao = new GenericDAO<>(Client.class);
            GenericDAO<Fournisseur, Long> fournisseurDao = new GenericDAO<>(Fournisseur.class);
            GenericDAO<User, Long> userDao = new GenericDAO<>(User.class);

            // -------------------------------------------------------------
            // TEST 1 : Création et Persistance des Catégories
            // -------------------------------------------------------------
            System.out.println("--- [TEST 1] Création de Catégories ---");
            Categorie catInfo = getOrCreateCategorie(catService, "Informatique");
            Categorie catElectronique = getOrCreateCategorie(catService, "Électronique");
            Categorie catAccessoires = getOrCreateCategorie(catService, "Accessoires");

            System.out.println("Catégorie obtenue : " + catInfo);
            System.out.println("Catégorie obtenue : " + catElectronique);
            System.out.println("Catégorie obtenue : " + catAccessoires);
            System.out.println("Total catégories en base : " + catService.getAll().size());
            System.out.println();

            // -------------------------------------------------------------
            // TEST 2 : Création de Produits et Relation ManyToOne
            // -------------------------------------------------------------
            System.out.println("--- [TEST 2] Création de Produits (ManyToOne) ---");
            Produit p1 = new Produit("PC Portable Gamer", new BigDecimal("1299.99"), catInfo);
            Produit p2 = new Produit("Souris Sans Fil", new BigDecimal("29.99"), catAccessoires);
            Produit p3 = new Produit("Clavier Mécanique", new BigDecimal("79.99"), catAccessoires);
            Produit p4 = new Produit("Smart TV 55 pouces", new BigDecimal("649.00"), catElectronique);

            produitService.create(p1);
            produitService.create(p2);
            produitService.create(p3);
            produitService.create(p4);

            System.out.println("Produits créés :");
            System.out.println(" - " + p1);
            System.out.println(" - " + p2);
            System.out.println(" - " + p3);
            System.out.println(" - " + p4);
            System.out.println();

            // -------------------------------------------------------------
            // TEST 3 : Named Queries (@NamedQuery)
            // -------------------------------------------------------------
            System.out.println("--- [TEST 3] Named Queries (@NamedQuery) ---");
            System.out.println("Produits de la catégorie 'Accessoires' (ID = " + catAccessoires.getId() + ") :");
            List<Produit> prodsParCat = produitService.findByCategorie(catAccessoires.getId());
            for (Produit p : prodsParCat) {
                System.out.println("  -> " + p.getLibelle() + " (" + p.getPrix() + " DH)");
            }
            System.out.println();

            // -------------------------------------------------------------
            // TEST 4 : Recherche dynamique (Criteria API)
            // -------------------------------------------------------------
            System.out.println("--- [TEST 4] Recherche dynamique via Criteria API ---");
            System.out.println("Recherche terme 'souris' :");
            List<Produit> search1 = produitService.search("souris", null, null);
            search1.forEach(p -> System.out.println("  Trouvé : " + p.getLibelle()));

            System.out.println("Recherche produits entre 20 et 100 DH :");
            List<Produit> search2 = produitService.search(null, new BigDecimal("20.00"), new BigDecimal("100.00"));
            search2.forEach(p -> System.out.println("  Trouvé : " + p.getLibelle() + " à " + p.getPrix() + " DH"));
            System.out.println();

            // -------------------------------------------------------------
            // TEST 5 : Pagination JPQL (setFirstResult / setMaxResults)
            // -------------------------------------------------------------
            System.out.println("--- [TEST 5] Pagination JPQL ---");
            int pageSize = 2;
            List<Produit> page1 = produitService.findPage(1, pageSize);
            System.out.println("Page 1 (taille = " + pageSize + ") :");
            page1.forEach(p -> System.out.println("  - [ID=" + p.getId() + "] " + p.getLibelle()));

            List<Produit> page2 = produitService.findPage(2, pageSize);
            System.out.println("Page 2 (taille = " + pageSize + ") :");
            page2.forEach(p -> System.out.println("  - [ID=" + p.getId() + "] " + p.getLibelle()));
            System.out.println();

            // -------------------------------------------------------------
            // TEST 6 : Chargement optimisé avec JOIN FETCH
            // -------------------------------------------------------------
            System.out.println("--- [TEST 6] Éviter LazyInitializationException avec JOIN FETCH ---");
            Produit produitWithCat = produitService.findWithCategorie(p1.getId());
            System.out.println("Produit : " + produitWithCat.getLibelle());
            System.out.println("Catégorie chargée immédiatement : " + produitWithCat.getCategorie().getNom());
            System.out.println();

            // -------------------------------------------------------------
            // TEST 7 : Héritage JPA - Stratégie JOINED
            // -------------------------------------------------------------
            System.out.println("--- [TEST 7] Héritage JPA (Stratégie JOINED) ---");
            Client client = new Client("Amine Bennani", LocalDate.now().minusMonths(6));
            clientDao.create(client);
            System.out.println("Client persisté : " + client);

            Fournisseur fournisseur = new Fournisseur("Tech Distribution SARL", "Casablanca, Boulevard Zerktouni");
            fournisseurDao.create(fournisseur);
            System.out.println("Fournisseur persisté : " + fournisseur);

            System.out.println("Polymorphisme : Tous les utilisateurs (User.findAll) :");
            List<User> tousLesUsers = userDao.findAll();
            for (User u : tousLesUsers) {
                System.out.println("  -> Type: " + u.getClass().getSimpleName() + " | Nom: " + u.getNom());
            }
            System.out.println();

            // -------------------------------------------------------------
            // TEST 8 : Relation N:N avec clé composée (Commandes & Lignes)
            // -------------------------------------------------------------
            System.out.println("--- [TEST 8] Relation N:N avec clé composée CommandeProduitPK ---");
            LocalDate dateMoisPrecedent = YearMonth.now().minusMonths(1).atDay(15);
            Commande cmd = new Commande(dateMoisPrecedent);
            cmd.addProduit(p1, 2); // 2 x PC Gamer
            cmd.addProduit(p2, 5); // 5 x Souris
            cmd.addProduit(p3, 3); // 3 x Clavier

            commandeService.create(cmd);
            System.out.println("Commande créée avec succès ! ID = " + cmd.getId());
            System.out.println("Nombre de lignes associées : " + cmd.getLignes().size());
            System.out.println();

            // -------------------------------------------------------------
            // TEST 9 : Rapport de Ventes Mensuel (Criteria API - sum & prod)
            // -------------------------------------------------------------
            System.out.println("--- [TEST 9] Rapport des Ventes Mensuel (Criteria API sum & prod) ---");
            List<VenteCategorieDTO> rapport = rapportService.getRapportVentesMoisPrecedent();
            System.out.printf("%-25s | %s%n", "Catégorie", "Chiffre d'Affaires");
            System.out.println("----------------------------------------------");
            BigDecimal totalCA = BigDecimal.ZERO;
            for (VenteCategorieDTO row : rapport) {
                System.out.printf("%-25s | %10.2f DH%n", row.getNomCategorie(), row.getChiffreAffaires());
                totalCA = totalCA.add(row.getChiffreAffaires());
            }
            System.out.println("----------------------------------------------");
            System.out.printf("%-25s | %10.2f DH%n", "TOTAL GLOBAL", totalCA);
            System.out.println();

            // -------------------------------------------------------------
            // TEST 10 : Gestion de transaction explicite et Rollback
            // -------------------------------------------------------------
            System.out.println("--- [TEST 10] Transaction explicite & Rollback en cas d'erreur ---");
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("defaultPU");
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Produit pInvalide = new Produit("Produit Erroné", new BigDecimal("-10.00"), catInfo);
                em.persist(pInvalide);

                if (pInvalide.getPrix().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Prix négatif interdit : " + pInvalide.getPrix());
                }
                tx.commit();
            } catch (Exception ex) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                System.out.println("Exception interceptée comme prévu : " + ex.getMessage());
                System.out.println("Rollback exécuté avec succès. Aucune donnée invalide insérée !");
            } finally {
                em.close();
                emf.close();
            }

            System.out.println("\n=========================================================");
            System.out.println("   TOUS LES TESTS SE SONT TERMINÉS AVEC SUCCÈS !");
            System.out.println("=========================================================");

        } catch (Throwable e) {
            System.err.println("\n[ERREUR LORS DE L'EXÉCUTION] : " + e.getMessage());
            e.printStackTrace();
            System.err.println("\nNote : Si l'erreur est liée à la connexion JDBC, assurez-vous que :");
            System.err.println("  1. Le serveur MySQL est démarré sur localhost:3306");
            System.err.println("  2. La base de données 'gestion_commerciale' existe (CREATE DATABASE IF NOT EXISTS gestion_commerciale;)");
            System.err.println("  3. Les identifiants dans persistence.xml / hibernate.cfg.xml correspondent à votre configuration.");
        }
    }

    private static Categorie getOrCreateCategorie(CrudService<Categorie, Long> catService, String nom) {
        return catService.getAll().stream()
                .filter(c -> nom.equalsIgnoreCase(c.getNom()))
                .findFirst()
                .orElseGet(() -> {
                    Categorie c = new Categorie(nom);
                    catService.create(c);
                    return c;
                });
    }
}