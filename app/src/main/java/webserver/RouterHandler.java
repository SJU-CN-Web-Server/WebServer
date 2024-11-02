package webserver;

import webserver.data.HttpRequest;
import webserver.data.HttpResponse;

public class RouterHandler extends HttpHandler{
    @Override
    public void process(HttpRequest request, HttpResponse response) {
        request.absPath = getAbsPath(request);
    }

    String getAbsPath(HttpRequest request) {
        String basePath = "./";
        return basePath + request.path;
    }
}
