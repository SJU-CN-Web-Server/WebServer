package webserver;

import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

import webserver.data.HttpRequest;
import webserver.data.HttpResponse;
public class RouterHandler extends HttpHandler {

    @Override
    public void process(HttpRequest request, HttpResponse response, Socket clientSocket) {
        request.absPath = getAbsPath(request);
        System.out.println("absPath: " + request.absPath);
    }
    
    String getAbsPath(HttpRequest request) {
        String basePath = System.getProperty("user.dir"); 
        Path path = Paths.get(basePath, request.path).toAbsolutePath();
        return path.toString();
    }

}
