package fr.pocarchi.tracabilite.infrastructure.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.pocarchi.tracabilite.application.ConsulterJournalService;
import fr.pocarchi.tracabilite.application.LancerTeleoperationService;
import fr.pocarchi.tracabilite.domain.CommandeTeleoperation;
import fr.pocarchi.tracabilite.domain.Prm;
import fr.pocarchi.tracabilite.infrastructure.securite.AuteurJeton;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/teleoperations")
class TeleoperationController {

    private final LancerTeleoperationService lancement;
    private final ConsulterJournalService journal;

    TeleoperationController(LancerTeleoperationService lancement, ConsulterJournalService journal) {
        this.lancement = lancement;
        this.journal = journal;
    }

    @PostMapping("/mono")
    @PreAuthorize("hasRole('OPERATEUR')")
    ResponseEntity<LancementResponse> lancer(@Valid @RequestBody LancementRequest requete, @AuthenticationPrincipal Jwt jwt) {
        CommandeTeleoperation commande = new CommandeTeleoperation(new Prm(requete.prm()), requete.service(), requete.parametres());
        String reference = lancement.lancer(commande, AuteurJeton.depuis(jwt));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new LancementResponse(reference));
    }

    @GetMapping("/journal")
    @PreAuthorize("hasAnyRole('OPERATEUR', 'LECTURE_SEULE')")
    List<EntreeJournalResponse> journal(@RequestParam String prm, @AuthenticationPrincipal Jwt jwt) {
        return journal.consulter(new Prm(prm), AuteurJeton.depuis(jwt)).stream()
            .map(EntreeJournalResponse::depuis)
            .toList();
    }
}
