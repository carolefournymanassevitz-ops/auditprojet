package fr.pocarchi.tracabilite;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Règles de couches de l'architecture (§5.3 : couches d'un module, règles vérifiées par ArchUnit),
 * vérifiées à chaque build plutôt que par convention.
 */
@AnalyzeClasses(packages = "fr.pocarchi.tracabilite", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule le_domaine_ne_depend_d_aucun_framework = noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta..", "java.sql..", "tools.jackson..");

    @ArchTest
    static final ArchRule l_application_ne_depend_pas_de_l_infrastructure = noClasses()
        .that().resideInAPackage("..application..")
        .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule le_web_n_appelle_jamais_un_adaptateur = noClasses()
        .that().resideInAPackage("..infrastructure.web..")
        .should().dependOnClassesThat().resideInAnyPackage("..infrastructure.persistence..", "..infrastructure.central..");

    @ArchTest
    static final ArchRule tout_point_d_entree_declare_un_role = methods()
        .that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
        .and().areMetaAnnotatedWith(RequestMapping.class)
        .should().beAnnotatedWith(PreAuthorize.class);
}
