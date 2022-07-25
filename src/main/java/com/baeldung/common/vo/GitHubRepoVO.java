package com.baeldung.common.vo;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record GitHubRepoVO(String repoName, String repoUrl, String repoLocalPath, String repoMasterHttpPath) {

    private static final Pattern REPO_URL_PATTERN = Pattern.compile(
        "https://github\\.com/(Baeldung|baeldung|eugenp)/(.*)/(tree|blob)/(master|[a-z\\d]+)/(.*)", Pattern.MULTILINE);

    public boolean canHandle(String url) {
        url = sanitizeUrl(url);
        // shortcut
        if (url.startsWith(repoMasterHttpPath) || repoUrl.equalsIgnoreCase(url + ".git")) {
            return true;
        }
        final Matcher matcher = REPO_URL_PATTERN.matcher(url);
        if (matcher.matches()) {
            // group 2 is repo name
            return this.repoName.equalsIgnoreCase(matcher.group(2));
        }
        return false;
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
        if (url.length() <= repoMasterHttpPath.length()) {
            return baseDir;
        }

        final Matcher matcher = REPO_URL_PATTERN.matcher(url);
        if (matcher.matches()) {
            String path = sanitizeUrl(matcher.group(5));
            return path.isEmpty() ? baseDir : baseDir.resolve(path);
        }
        return null;
    }

    /**
     * Method does two things:
     * <ul>
     *     <li>clears last slash if any, like: "module/"</li>
     *     <li>clears url fragments if any, like: "module#readme"</li>
     * </ul>
     */
    private String sanitizeUrl(String url) {
        // clear last "/"
        url = (!url.isEmpty() && url.lastIndexOf("/") == url.length() - 1) ? url.substring(0, url.lastIndexOf("/")) : url;
        // clear url fragments, like: #readme
        return url.contains("#") ? url.substring(0, url.indexOf("#")) : url;
    }

}
