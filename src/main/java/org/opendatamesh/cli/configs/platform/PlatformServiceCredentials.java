package org.opendatamesh.cli.configs.platform;


import org.opendatamesh.cli.configs.generic.GenericConnectionHeader;
import org.opendatamesh.cli.configs.generic.Oauth2;

import java.util.List;

public class PlatformServiceCredentials {
    private String url;
    private List<GenericConnectionHeader> headers;
    private Oauth2 oauth2;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<GenericConnectionHeader> getHeaders() {
        return headers;
    }

    public void setHeaders(List<GenericConnectionHeader> headers) {
        this.headers = headers;
    }

    public Oauth2 getOauth2() {
        return oauth2;
    }

    public void setOauth2(Oauth2 oauth2) {
        this.oauth2 = oauth2;
    }
}


