package webserver;

import java.net.Socket;

import webserver.data.HttpRequest;
import webserver.data.HttpResponse;

public class RouterHandler extends HttpHandler{
    @Override
    public void process(HttpRequest request, HttpResponse response, Socket clientSocket) {
        request.absPath = getAbsPath(request);
    }

    String getAbsPath(HttpRequest request) {
        String basePath = System.getProperty("user.dir");
        return basePath + request.path;
    }
}