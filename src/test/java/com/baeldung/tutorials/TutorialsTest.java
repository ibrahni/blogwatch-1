package com.baeldung.tutorials;

import com.baeldung.common.*;
import static com.baeldung.common.ConsoleColors.*;
import com.baeldung.common.config.CommonConfig;
import com.baeldung.common.config.MyApplicationContextInitializer;
import com.baeldung.common.vo.MavenProjectVO;
import com.baeldung.filevisitor.MavenModulesDetailsFileVisitor;
import com.baeldung.utility.TestUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.baeldung.common.GlobalConstants.*;
import static com.baeldung.common.GlobalConstants.TestMetricTypes.FAILED;
import static com.baeldung.common.Utils.*;

@ContextConfiguration(classes = {CommonConfig.class}, initializers = MyApplicationContextInitializer.class)
@ExtendWith(TestMetricsExtension.class)
@ExtendWith(SpringExtension.class)
public class TutorialsTest extends BaseTest {

    @Test
    @Tag(GlobalConstants.TAG_SKIP_METRICS)
    public void givenTheTutorialsRepository_listAllTheModulesThatAreNotBuildInBothDefautlAndIntegrationTests(TestInfo testInfo) throws IOException, GitAPIException, XmlPullParserException {
        recordExecution(GlobalConstants.givenTheTutorialsRepository_listAllTheModulesThatAreNotBuildInBothDefautlAndIntegrationTests);

        List<String> testExceptions = getTestExceptions(testInfo);

        Path repoLocalPath = Paths.get(tutorialsRepoLocalPath);
        Utils.fetchGitRepo(GlobalConstants.YES, repoLocalPath, tutorialsRepoGitUrl);

        MavenModulesDetailsFileVisitor modulesFileVisitor = new MavenModulesDetailsFileVisitor(GlobalConstants.tutorialsRepoLocalPath);
        Files.walkFileTree(repoLocalPath, modulesFileVisitor);

        Map<String, MavenProjectVO> modules = modulesFileVisitor.getModules();
        buildParentChildRelation(modules);

        HashMap<String, List<String>> defaultProfiles = new HashMap<>();
        HashMap<String, List<String>> integrationProfiles = new HashMap<>();
        extractModulesForProfile(defaultProfiles, integrationProfiles, tutorialsRepoLocalPath);

        markBuiltModules(modules, defaultProfiles, true);
        markBuiltModules(modules, integrationProfiles, false);

        List<MavenProjectVO> modulesMissingInDefault = modules.values().stream()
                .filter(module -> !module.isBuildInDefaultProfile()
                        && !testExceptions.contains(removeRepoLocalPath(module.getPomFileLocation())))
                .sorted(Comparator.comparing(MavenProjectVO::getPomFileLocation))
                .collect(Collectors.toList());

        List<MavenProjectVO> modulesMissingInIntegraiton = modules.values().stream()
                .filter(module -> !module.isBuildInIntegrationProfile()
                        && !testExceptions.contains(removeRepoLocalPath(module.getPomFileLocation())))
                .sorted(Comparator.comparing(MavenProjectVO::getPomFileLocation))
                .collect(Collectors.toList());


        if (!modulesMissingInDefault.isEmpty() || !modulesMissingInIntegraiton.isEmpty()) {
            String results = getErrorMessageForNotBuiltModules(modulesMissingInDefault, modulesMissingInIntegraiton);
            int totalFailures = modulesMissingInDefault.size() + modulesMissingInIntegraiton.size();

            BaseTest.recordMetrics(totalFailures, FAILED);
            recordFailure(GlobalConstants.givenTheTutorialsRepository_listAllTheModulesThatAreNotBuildInBothDefautlAndIntegrationTests, totalFailures);
            triggerTestFailure(results, "Not all modules are built.");
        }

        logger.info(ConsoleColors.magentaColordMessage("finished"));
    }

    private List<String> getTestExceptions(TestInfo testInfo) {
        List<String> testExceptions= YAMLProperties.exceptionsForTests.get(TestUtils.getMehodName(testInfo.getTestMethod()));
        if (testExceptions == null) {
            testExceptions = new ArrayList<>();
        }
        return testExceptions;
    }

    private void markBuiltModules(Map<String, MavenProjectVO> allModules, HashMap<String, List<String>> modulesForProfiles, boolean defaultProfiles) {
        for (Map.Entry<String, List<String>> modulesForProfile : modulesForProfiles.entrySet()) {
            for (String module : modulesForProfile.getValue()) {
                logger.info(magentaColordMessage("Processing moduele: {}"), module);
                String artifactId = getArtifactId(module);
                MavenProjectVO mavenProject = allModules.get(artifactId);
                if (null == mavenProject) {
                    logger.error(ConsoleColors.redBoldMessage("Couldn't retrieve MavenProjectVO for {}, perhaps module directory and artifactId don't match"), module);
                    continue;
                }
                markBuiltHierarchy(mavenProject, defaultProfiles);
            }
        }
    }

    private String getArtifactId(String module) {
        return module.contains("/") ? module.substring(module.lastIndexOf("/") + 1) : module;
    }

    private void markBuiltHierarchy(MavenProjectVO mavenProjectVO, boolean defaultProfiles) {
        if (defaultProfiles) {
            mavenProjectVO.setBuildInDefaultProfile(true);
        } else {
            mavenProjectVO.setBuildInIntegrationProfile(true);
        }

        for (MavenProjectVO child : mavenProjectVO.getChildren()) {
            markBuiltHierarchy(child, defaultProfiles);
        }
    }


    private void buildParentChildRelation(Map<String, MavenProjectVO> allModules) {
        for (MavenProjectVO module : allModules.values()) {

            for (String childArtifactId : module.getChildModules()) {
                childArtifactId = getArtifactId(childArtifactId);

                MavenProjectVO childModule = allModules.get(childArtifactId);
                if (childModule == null) {
                    logger.warn("CHILD MODULE NOT FOUND BY ARTIFACT ID: " + childArtifactId);
                    childModule = findModuleByPath(childArtifactId, allModules);
                }

                if (childModule == null) {
                    logger.warn("CHILD MODULE NOT FOUND BY PATH: " + childArtifactId);
                } else {
                    childModule.setParent(module);
                    module.getChildren().add(childModule);
                }
            }

        }
    }

    private MavenProjectVO findModuleByPath(String childFolder, Map<String, MavenProjectVO> modules) {
        for (MavenProjectVO module : modules.values()) {
            String pomFileLocation = module.getPomFileLocation();
            if (pomFileLocation.replace("/" + POM_FILE_NAME_LOWERCASE, "").endsWith(childFolder)) {
                return module;
            }
        }

        return null;
    }

    private void extractModulesForProfile(Map<String, List<String>> defaultProfiles, Map<String, List<String>> integrationProfiles, String repoLocalPath) throws IOException, XmlPullParserException {
        File parentPom = new File(repoLocalPath + "/" + POM_FILE_NAME_LOWERCASE);

        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model pomModel = reader.read(new FileReader(parentPom));
        for (Profile profile : pomModel.getProfiles()) {
            String id = profile.getId();
            if (id.startsWith("default-")) {
                defaultProfiles.put(id, profile.getModules());
            } else if (id.startsWith("integration-")) {
                integrationProfiles.put(id, profile.getModules());
            }
        }
    }
}
