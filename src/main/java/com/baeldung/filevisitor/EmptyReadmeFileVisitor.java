package com.baeldung.filevisitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baeldung.common.GlobalConstants;
import com.baeldung.common.Utils;
import com.baeldung.common.YAMLProperties;

public class EmptyReadmeFileVisitor extends SimpleFileVisitor<Path> {

    private String repoLocalPath;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private List<String> emptyReadmeList = new ArrayList<>();
    private List<String> missingReadmeList = new ArrayList<>();

    public EmptyReadmeFileVisitor(String repoLocalPath) {
        super();
        this.repoLocalPath = repoLocalPath;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (dir.toString()
            .equalsIgnoreCase(repoLocalPath + "/.git/")
            || dir.toString()
                .equalsIgnoreCase(repoLocalPath + "/.git")) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {

        String pathAsString = path.toString();        
        if (Utils.excludePage(pathAsString, YAMLProperties.exceptionsForEmptyReadmeTest.get(GlobalConstants.IGNORE_EMPTY_README_CONTAINING_LIST_KEY), (theCurrentUrl, anEntryIntheList) -> theCurrentUrl.contains(anEntryIntheList))
            || Utils.excludePage(pathAsString, YAMLProperties.exceptionsForEmptyReadmeTest.get(GlobalConstants.IGNORE_MISSING_README_CONTAINING_LIST_KEY), (theCurrentUrl, anEntryIntheList) -> theCurrentUrl.contains(anEntryIntheList))
            || Utils.excludePage(pathAsString, YAMLProperties.exceptionsForEmptyReadmeTest.get(GlobalConstants.IGNORE_EMPTY_README_ENDING_WITH_LIST_KEY), (theCurrentUrl, anEntryIntheList) -> theCurrentUrl.endsWith(anEntryIntheList))
            || Utils.excludePage(pathAsString, YAMLProperties.exceptionsForEmptyReadmeTest.get(GlobalConstants.IGNORE_MISSING_README_ENDING_WITH_LIST_KEY), (theCurrentUrl, anEntryIntheList) -> theCurrentUrl.endsWith(anEntryIntheList))) {
            logger.info("skipping as it's in the exception list {}", path);
            return FileVisitResult.CONTINUE;
        }

        File file = path.toFile();

        if (file.isFile() && file.getName()
            .toLowerCase()
            .endsWith(GlobalConstants.POM_FILE_NAME_LOWERCASE)) {
            String expectedReadmePath = path.getParent()
                .toString()
                .concat("/")
                .concat(GlobalConstants.README_FILE_NAME_UPPERCASE);           
            if (!Files.exists(Paths.get(expectedReadmePath))) {
                logger.info("module found with missing readme {}", path);
                missingReadmeList.add(path.getParent()
                    .toString());
            }

            if (file.isFile() && file.getName()
                .toLowerCase()
                .endsWith(GlobalConstants.README_FILE_NAME_LOWERCASE)) {
                int baeldungUrlsCount = Utils.getLinksToTheBaeldungSite(file.getAbsolutePath()); // get all the articles
                                                                                                 // linked in this README
                if (baeldungUrlsCount == 0) {
                    logger.info("empty redme found {}", path);
                    emptyReadmeList.add(path.toString());
                }
            }

        }

        return FileVisitResult.CONTINUE;
    }

    public List<String> getEmptyReadmeList() {
        return emptyReadmeList;
    }

    public void setEmptyReadmeList(List<String> emptyReadmeList) {
        this.emptyReadmeList = emptyReadmeList;
    }

    public List<String> getMissingReadmeList() {
        return missingReadmeList;
    }

    public void setMissingReadmeList(List<String> missingReadmeList) {
        this.missingReadmeList = missingReadmeList;
    }

}
