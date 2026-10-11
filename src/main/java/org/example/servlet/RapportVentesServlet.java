package org.example.servlet;

import org.example.dto.VenteCategorieDTO;
import org.example.service.RapportVentesService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet("/rapport-ventes")
public class RapportVentesServlet extends HttpServlet {
    private final RapportVentesService rapportService = new RapportVentesService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        YearMonth moisPrecedent = YearMonth.now().minusMonths(1);
        String moisFormate = moisPrecedent.format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        List<VenteCategorieDTO> rapport = rapportService.getRapportVentesMoisPrecedent();

        BigDecimal caTotalGlobal = rapport.stream()
                .map(VenteCategorieDTO::getChiffreAffaires)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang='fr'>");
            out.println("<head>");
            out.println("  <meta charset='UTF-8'>");
            out.println("  <title>Rapport des Ventes Mensuel</title>");
            out.println("  <style>");
            out.println("    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8fafc; margin: 40px; color: #1e293b; }");
            out.println("    .container { max-width: 800px; margin: auto; background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }");
            out.println("    h1 { color: #0f172a; border-bottom: 2px solid #e2e8f0; padding-bottom: 12px; }");
            out.println("    .subtitle { color: #64748b; font-size: 1.1em; margin-bottom: 24px; }");
            out.println("    table { width: 100%; border-collapse: collapse; margin-top: 16px; }");
            out.println("    th, td { text-align: left; padding: 12px 16px; border-bottom: 1px solid #e2e8f0; }");
            out.println("    th { background-color: #f1f5f9; color: #334155; font-weight: 600; }");
            out.println("    tr:hover { background-color: #f8fafc; }");
            out.println("    .montant { text-align: right; font-weight: 600; color: #0284c7; }");
            out.println("    .total-row { font-weight: bold; background-color: #e0f2fe; }");
            out.println("    .total-row td { border-top: 2px solid #0284c7; }");
            out.println("  </style>");
            out.println("</head>");
            out.println("<body>");
            out.println("  <div class='container'>");
            out.println("    <h1>Rapport des Ventes Mensuel</h1>");
            out.println("    <p class='subtitle'>Chiffre d'affaires par catégorie pour <strong>" + moisFormate + "</strong></p>");
            out.println("    <table>");
            out.println("      <thead>");
            out.println("        <tr>");
            out.println("          <th>Catégorie</th>");
            out.println("          <th style='text-align: right;'>Chiffre d'Affaires</th>");
            out.println("        </tr>");
            out.println("      </thead>");
            out.println("      <tbody>");

            if (rapport.isEmpty()) {
                out.println("        <tr><td colspan='2' style='text-align:center; color: #94a3b8;'>Aucune vente enregistrée pour ce mois.</td></tr>");
            } else {
                for (VenteCategorieDTO dto : rapport) {
                    out.println("        <tr>");
                    out.println("          <td>" + dto.getNomCategorie() + "</td>");
                    out.println("          <td class='montant'>" + String.format("%.2f DH", dto.getChiffreAffaires()) + "</td>");
                    out.println("        </tr>");
                }
            }

            out.println("      </tbody>");
            out.println("      <tfoot>");
            out.println("        <tr class='total-row'>");
            out.println("          <td>Total Global</td>");
            out.println("          <td class='montant'>" + String.format("%.2f DH", caTotalGlobal) + "</td>");
            out.println("        </tr>");
            out.println("      </tfoot>");
            out.println("    </table>");
            out.println("  </div>");
            out.println("</body>");
            out.println("</html>");
        }
    }
}
