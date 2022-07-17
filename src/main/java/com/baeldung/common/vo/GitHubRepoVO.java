package com.baeldung.common.vo;

import java.nio.file.Path;
import java.nio.file.Paths;

public record GitHubRepoVO(String repoName, String repoUrl, String repoLocalPath, String repoMasterHttpPath) {

    public boolean canHandle(String url) {
        return url.startsWith(repoMasterHttpPath) || repoUrl.equals(url + ".git");
    }

    /**
     * Takes Github url and finds corresponding local directory
     *
     * @param url Github HTTP url
     * @return Path of directory or null
     */
    public Path getLocalPathByUrl(String url) {
        if (!canHandle(url))
            return null;
        Path baseDir = Path.of(repoLocalPath);
        if (url.length() < repoMasterHttpPath.length()) {
            return baseDir;
        }
        String path = url.substring(url.indexOf(repoMasterHttpPath) + repoMasterHttpPath.length());
        if (path.isEmpty() || path.equals("/")) {
            return baseDir;
        }
        return baseDir.resolve(path.substring(1));
    }

}
