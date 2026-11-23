package org.opendatamesh.cli.usecases.devops;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Structure test of the module {@code usecases/devops} (PROGRESSIVE_DISCOVERY). Part of BDMD-5443, see the
 * prompt in {@code spdd/prompt/}. A plain unit test: it imports the compiled classes and starts no application.
 */
class DevOpsUseCasesStructureTest {

    private static final String MODULE = "org.opendatamesh.cli.usecases.devops";
    private static final String START_TASK = MODULE + ".starttask";
    private static final String COMPLETE_TASK = MODULE + ".completetask";
    private static final String[] STEP_CLASSES = {
            START_TASK + ".OpenActivity", START_TASK + ".VersionLookup", START_TASK + ".ActivityPlan", START_TASK + ".Approval",
            COMPLETE_TASK + ".TaskReportCheck"
    };

    private static com.tngtech.archunit.core.domain.JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("org.opendatamesh.cli");
    }

    @Test
    void unitsExposeOnlyTheirEntryTypes() {
        // The step enum is public because the presenter's methods take it.
        classes().that().resideInAnyPackage(START_TASK, COMPLETE_TASK)
                .and().arePublic().and().areTopLevelClasses()
                .should().haveSimpleNameEndingWith("Factory")
                .orShould().haveSimpleNameEndingWith("Command")
                .orShould().haveSimpleNameEndingWith("Presenter")
                .orShould().haveSimpleNameEndingWith("Step")
                .check(classes);
    }

    @Test
    void nothingOutsideAUnitDependsOnItsOtherTypes() {
        for (String unit : List.of(START_TASK, COMPLETE_TASK)) {
            classes().that().resideInAPackage(unit).and().areTopLevelClasses()
                    .and().haveSimpleNameNotEndingWith("Factory")
                    .and().haveSimpleNameNotEndingWith("Command")
                    .and().haveSimpleNameNotEndingWith("Presenter")
                    .and().haveSimpleNameNotEndingWith("Step")
                    .should().onlyHaveDependentClassesThat().resideInAPackage(unit)
                    .check(classes);
        }
    }

    @Test
    void unitsDoNotDependOnEachOther() {
        noClasses().that().resideInAPackage(START_TASK).should().dependOnClassesThat().resideInAPackage(COMPLETE_TASK).check(classes);
        noClasses().that().resideInAPackage(COMPLETE_TASK).should().dependOnClassesThat().resideInAPackage(START_TASK).check(classes);
    }

    @Test
    void stepClassesDoNotDependOnAdaptersClientImplementationsOrFrameworks() {
        for (String stepClass : STEP_CLASSES) {
            noClasses().that().haveFullyQualifiedName(stepClass)
                    .should().dependOnClassesThat().haveSimpleNameEndingWith("OutboundPortImpl")
                    .orShould().dependOnClassesThat().haveSimpleNameEndingWith("ClientImpl")
                    .orShould().dependOnClassesThat().haveSimpleNameEndingWith("ClientFactory")
                    .orShould().dependOnClassesThat().resideInAnyPackage("org.springframework..", "picocli..")
                    .check(classes);
        }
    }

    @Test
    void outboundPortImplementationsAreBuiltOnlyByTheUnitFactory() {
        for (String unit : List.of(START_TASK, COMPLETE_TASK)) {
            classes().that().resideInAPackage(unit).and().haveSimpleNameEndingWith("OutboundPortImpl")
                    .should().onlyHaveDependentClassesThat(factoryOrOwnNestedClass())
                    .check(classes);
        }
    }

    @Test
    void noHelperOrUtilityTypes() {
        noClasses().that().resideInAPackage(MODULE + "..")
                .should().haveSimpleNameEndingWith("Helper")
                .orShould().haveSimpleNameEndingWith("Util")
                .orShould().haveSimpleNameEndingWith("Utils")
                .check(classes);
    }

    private static com.tngtech.archunit.base.DescribedPredicate<JavaClass> factoryOrOwnNestedClass() {
        return new com.tngtech.archunit.base.DescribedPredicate<>("are a *Factory or a nested class of an *OutboundPortImpl") {
            @Override
            public boolean test(JavaClass javaClass) {
                return javaClass.getSimpleName().endsWith("Factory") || javaClass.getName().contains("OutboundPortImpl");
            }
        };
    }
}
