package org.opendatamesh.cli.clients.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.io.File;

public abstract class AuthenticatedRestUtils extends RestUtils {

    protected AuthenticatedRestUtils(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public <R, F> Page<R> getPage(String url, HttpHeaders httpHeaders, Pageable pageable, F filters, Class<R> clazz) throws ClientException, ClientResourceMappingException {
        return super.getPage(url, getAuthenticatedHeaders(httpHeaders), pageable, filters, clazz);
    }

    @Override
    public <R, ID> R get(String url, HttpHeaders httpHeaders, ID identifier, Class<R> clazz) throws ClientException, ClientResourceMappingException {
        return super.get(url, getAuthenticatedHeaders(httpHeaders), identifier, clazz);
    }

    @Override
    public <R> R create(String url, HttpHeaders httpHeaders, R resourceToCreate, Class<R> clazz) throws ClientException, ClientResourceMappingException {
        return super.create(url, getAuthenticatedHeaders(httpHeaders), resourceToCreate, clazz);
    }

    @Override
    public <R, ID> R put(String url, HttpHeaders httpHeaders, ID identifier, R resourceToModify, Class<R> clazz) throws ClientException, ClientResourceMappingException {
        return super.put(url, getAuthenticatedHeaders(httpHeaders), identifier, resourceToModify, clazz);
    }

    @Override
    public <R, ID> R patch(String url, HttpHeaders httpHeaders, ID identifier, R resourceToModify, Class<R> clazz) throws ClientException, ClientResourceMappingException {
        return super.patch(url, getAuthenticatedHeaders(httpHeaders), identifier, resourceToModify, clazz);
    }

    @Override
    public <ID> void delete(String url, HttpHeaders httpHeaders, ID identifier) throws ClientException {
        super.delete(url, getAuthenticatedHeaders(httpHeaders), identifier);
    }

    @Override
    public <I, O> O genericPost(String url, HttpHeaders httpHeaders, I resource, Class<O> clazz) throws ClientException, ClientResourceMappingException {
        return super.genericPost(url, getAuthenticatedHeaders(httpHeaders), resource, clazz);
    }

    @Override
    public File download(String url, HttpHeaders httpHeaders, Object resource, File storeLocation) throws ClientException, ClientResourceMappingException {
        return super.download(url, getAuthenticatedHeaders(httpHeaders), resource, storeLocation);
    }

    protected abstract HttpHeaders getAuthenticatedHeaders(HttpHeaders headers);
}
