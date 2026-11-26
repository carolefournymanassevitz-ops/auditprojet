package fr.pocarchi.tracabilite.application.port;

import java.util.Set;

/**
 * Propriétaires visibles pour une désignation, délégations comprises (RG-10 : un utilisateur ne voit et n'agit que
 * sur les équipements dont son ELD est propriétaire ou gestionnaire).
 */
public interface PerimetrePort {

    Set<String> proprietaires(String designation);
}
