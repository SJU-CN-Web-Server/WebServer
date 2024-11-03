package webserver;

import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

import webserver.data.HttpRequest;
import webserver.data.HttpResponse;

public class RouterHandler extends HttpHandler{
    @Override
    public void process(HttpRequest request, HttpResponse response, Socket clientSocket) {
        request.absPath = getAbsPath(request);
    }

    String getAbsPath(HttpRequest request) {
        String basePath = "./";
        Path path = Paths.get(basePath, request.path == null ? "" : request.path, "hello.txt");
        return path.toString();
    }
}
