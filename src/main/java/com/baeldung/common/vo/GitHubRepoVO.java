package com.baeldung.common.vo;

import java.nio.file.Path;
import java.nio.file.Paths;

public record GitHubRepoVO(String repoName, String repoUrl, String repoLocalPath, String repoMasterHttpPath) {

    public boolean canHandle(String url) {
        return url.startsWith(repoMasterHttpPath);
    }

    /**
     * Takes Github url and finds corresponding local directory
     *
     * @param url Github HTTP url
     * @return Path of directory or null
     */
    public Path getLocalPathByUrl(String url) {
        if (canHandle(url)) {
            final int index = url.indexOf(repoMasterHttpPath) + repoMasterHttpPath.length() + 1;
            if (index > url.length()) {
                return null;
            }
            String path = url.substring(index);
            return Paths.get(repoLocalPath())
                .resolve(path);
        }
        return null;
    }

}
