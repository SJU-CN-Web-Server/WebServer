package webserver;


import java.net.Socket;

import webserver.data.HttpRequest;
import webserver.data.HttpResponse;

public abstract class HttpHandler {
    private HttpHandler nextHandler;
    private static boolean goToResponse = false;
    
    public abstract void process(HttpRequest request, HttpResponse response, Socket connectionSocket);

    public static boolean isgoToResponse() {
        return goToResponse;
    }

    public void handle(HttpRequest request, HttpResponse response, Socket connectionSocket){
        if(!isgoToResponse()){
            process(request, response, connectionSocket);
        }
        if(nextHandler != null){
            nextHandler.handle(request, response, connectionSocket);
        } else process(request, response, connectionSocket);   
    }

    public HttpHandler setNextHandler(HttpHandler nextHandler) {
        this.nextHandler = nextHandler;
        return this.nextHandler;
    }

    public static void setGoToResponse(boolean _goToResponse) {
        goToResponse = _goToResponse;
    }
}
