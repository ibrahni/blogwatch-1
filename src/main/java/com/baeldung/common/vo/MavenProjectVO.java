package com.baeldung.common.vo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MavenProjectVO {

    private String groupId;
    private String artifactId;
    private String version;

    private String pomFileLocation;

    private boolean buildInDefaultProfile;
    private boolean buildInIntegrationProfile;

    private List<String> childModules;

    private MavenProjectVO parent;
    private Set<MavenProjectVO> children = new HashSet<>();

    public MavenProjectVO(String groupId, String artifactId, String version, String pomFileLocation) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.pomFileLocation = pomFileLocation;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isBuildInDefaultProfile() {
        return buildInDefaultProfile;
    }

    public void setBuildInDefaultProfile(boolean buildInDefaultProfile) {
        this.buildInDefaultProfile = buildInDefaultProfile;
    }

    public boolean isBuildInIntegrationProfile() {
        return buildInIntegrationProfile;
    }

    public void setBuildInIntegrationProfile(boolean buildInIntegrationProfile) {
        this.buildInIntegrationProfile = buildInIntegrationProfile;
    }

    public MavenProjectVO getParent() {
        return parent;
    }

    public void setParent(MavenProjectVO parent) {
        this.parent = parent;
    }

    public Set<MavenProjectVO> getChildren() {
        return children;
    }

    public void setChildren(Set<MavenProjectVO> children) {
        this.children = children;
    }

    public String getPomFileLocation() {
        return pomFileLocation;
    }

    public void setPomFileLocation(String pomFileLocation) {
        this.pomFileLocation = pomFileLocation;
    }

    public List<String> getChildModules() {
        return childModules;
    }

    public void setChildModules(List<String> childModules) {
        this.childModules = childModules;
    }
}
