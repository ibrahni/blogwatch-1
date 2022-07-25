package com.baeldung.common.vo;

public class LinkVO {

    String link;
    String linkText;

    public LinkVO(String link, String linkText) {
        super();
        this.link = link;
        this.linkText = linkText;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLinkText() {
        return linkText;
    }

    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }

    @Override
    public String toString() {
        return "\n" + linkText + "( " + link + " )";
    }

}
