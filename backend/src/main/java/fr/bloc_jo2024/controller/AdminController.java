package fr.bloc_jo2024.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminController {

    // Le mot de passe administrateur est défini en dur via les propriétés
    @Value("${admin.password}")
    private String adminPassword;

    // Affiche la page de connexion administrateur.

    @GetMapping("/admin/login")
    public String adminLoginPage() {
        return "admin-login"; // Renvoie au template admin-login.html
    }

    /**
     * Traitement de la connexion administrateur.
     * Si le mot de passe est correct, un attribut de session "ADMIN_LOGGED_IN" est défini.
     * Sinon, on renvoie sur la page de login avec un message d'erreur.
     *
     * @param password Mot de passe saisi par l'administrateur.
     * @param request  Permet de gérer la session.
     * @param model    Pour ajouter un message d'erreur éventuel.
     * @return Redirection vers le dashboard admin en cas de succès, ou la page login en cas d'erreur.
     */
    @PostMapping("/admin/login")
    public String adminLogin(@RequestParam("password") String password, HttpServletRequest request, Model model) {
        if (password.equals(adminPassword)) {

            // Connexion réussie : on définit le flag dans la session.
            HttpSession session = request.getSession();
            session.setAttribute("ADMIN_LOGGED_IN", true);

            // Redirige vers le dashboard
            return "redirect:/admin/dashboard";
        } else {

            // En cas d'erreur, on renvoie sur le login avec un message
            model.addAttribute("error", "Mot de passe invalide.");
            return "admin-login";
        }
    }

    /**
     * Déconnexion de l'administrateur.
     * Invalide la session en cours.
     *
     * @param request Pour accéder à la session.
     * @return Redirige vers la page de connexion.
     */
    @PostMapping("/admin/logout")
    public String adminLogout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/admin/login?logout";
    }

    /**
     * Affiche la page du dashboard administrateur.
     * Si l'administrateur n'est pas connecté (flag absent dans la session), il est redirigé vers la page de login.
     *
     * @param request Pour vérifier la session.
     * @return Le template du dashboard admin.
     */
    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("ADMIN_LOGGED_IN") == null) {
            return "redirect:/admin/login";
        }
        return "admin-dashboard";
    }
}
