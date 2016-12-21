package org.iobis.reaper;

import java.util.Date;

public class IPTResource {

    private String url;
    private String description;
    private String title;
    private String dwca;
    private String eml;
    private Date date;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDwca() {
        return dwca;
    }

    public void setDwca(String dwca) {
        this.dwca = dwca;
    }

    public String getEml() {
        return eml;
    }

    public void setEml(String eml) {
        this.eml = eml;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
