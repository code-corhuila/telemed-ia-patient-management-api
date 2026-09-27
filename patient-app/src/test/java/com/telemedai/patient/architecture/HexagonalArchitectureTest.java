package com.telemedai.patient.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Enforces the hexagonal architecture dependency rules.
 *
 * The domain layer must not depend on Spring, JPA, or any other framework.
 * The application layer must not depend on Spring either.
 * Dependencies point inward: infrastructure → application → domain.
 */
class HexagonalArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.telemedai.patient");
    }

    @Test
    @DisplayName("domain must not depend on Spring")
    void domainShouldNotDependOnSpring() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
                .check(classes);
    }

    @Test
    @DisplayName("domain must not depend on JPA")
    void domainShouldNotDependOnJpa() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("jakarta.persistence..", "javax.persistence..")
                .check(classes);
    }

    @Test
    @DisplayName("domain must not depend on application or infrastructure")
    void domainShouldNotDependOnOuterLayers() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..application..", "..infrastructure..")
                .check(classes);
    }

    @Test
    @DisplayName("application must not depend on Spring or JPA")
    void applicationShouldNotDependOnFrameworks() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "javax.persistence.."
                )
                .check(classes);
    }

    @Test
    @DisplayName("application must not depend on infrastructure")
    void applicationShouldNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..")
                .check(classes);
    }
}
