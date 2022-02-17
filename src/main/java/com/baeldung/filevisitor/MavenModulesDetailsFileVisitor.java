package com.baeldung.filevisitor;

import com.baeldung.common.vo.MavenProjectVO;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class MavenModulesDetailsFileVisitor extends SimpleFileVisitor<Path> {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, MavenProjectVO> modules;

    private String repoPath;

    public MavenModulesDetailsFileVisitor(String repoPath) {
        modules = new HashMap<>();
        this.repoPath = repoPath;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (dir.toString().equalsIgnoreCase(repoPath + "/.git/") || dir.toString().equalsIgnoreCase(repoPath+ "/.git")) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {

        // skip parrent pom
        if (repoPath.concat("/pom.xml").equals(path.toString())) {
            return FileVisitResult.CONTINUE;
        }

        File file = path.toFile();
        if (file.isFile() && file.getName().equalsIgnoreCase("pom.xml")) {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            try {
                Model pomModel = reader.read(new FileReader(path.toString()));
                pomModel.setPomFile(file);

                MavenProjectVO project = new MavenProjectVO(pomModel.getGroupId(), pomModel.getArtifactId(), pomModel.getVersion(), path.toString());
                project.setChildModules(pomModel.getModules());
                modules.put(pomModel.getArtifactId(), project);

            } catch (XmlPullParserException e) {
                logger.error("Error while parsing POM" + path.toString());
            }
        }
        return FileVisitResult.CONTINUE;
    }

    public Map<String, MavenProjectVO> getModules() {
        return modules;
    }
}
