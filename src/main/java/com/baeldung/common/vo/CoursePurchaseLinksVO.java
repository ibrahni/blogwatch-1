package com.baeldung.common.vo;

import java.util.List;

import org.apache.commons.lang3.BooleanUtils;

public class CoursePurchaseLinksVO {
    private String courseUrl;
    private List<PurchaseLink> purchaseLinks;
    private Boolean isForTeams;

    public String getCourseUrl() {
        return courseUrl;
    }

    public void setCourseUrl(String courseUrl) {
        this.courseUrl = courseUrl;
    }

    public List<PurchaseLink> getPurchaseLinks() {
        return purchaseLinks;
    }

    public void setPurchaseLinks(List<PurchaseLink> purchaseLinks) {
        this.purchaseLinks = purchaseLinks;
    }

    public boolean isForTeams(){
        return BooleanUtils.isTrue(isForTeams);
    }

    public void setIsForTeams(Boolean isForTeams){
        this.isForTeams = isForTeams;
    }
    
    public static class PurchaseLink {
        private String linkId;
        private String link;
        private String redirectsTo;
        public String getLinkId() {
            return linkId;
        }
        public void setLinkId(String linkId) {
            this.linkId = linkId;
        }
        public String getLink() {
            return link;
        }
        public void setLink(String link) {
            this.link = link;
        }
        public String getRedirectsTo() {
            return redirectsTo;
        }
        public void setRedirectsTo(String redirectsTo) {
            this.redirectsTo = redirectsTo;
        }                            
    }

}
