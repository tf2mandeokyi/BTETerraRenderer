package com.mndk.bteterrarenderer.connector.terraplusplus;

import com.mndk.bteterrarenderer.connector.ImplFinder;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public interface HttpConnector {
    HttpConnector INSTANCE = ImplFinder.search(HttpConnector.class);

    InputStream download(String url) throws ExecutionException, InterruptedException;
}
