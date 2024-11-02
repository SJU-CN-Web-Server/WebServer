package webserver;

import java.net.*;
import webserver.data.*;

public abstract class HttpHandler {
    private HttpHandler nextHandler;
    private boolean goToResponse = false;
    
    public abstract void process(HttpRequest request, HttpResponse response, Socket clientSocket);

    public void handle(HttpRequest request, HttpResponse response, Socket clientSocket){
        process(request, response, clientSocket);
        //추가 구현예정
        if(goToResponse){
            return;
        }
        if(!goToResponse && nextHandler != null){
            nextHandler.handle(request, response, clientSocket);
        }
    }

    public HttpHandler setNextHandler(HttpHandler nextHandler) {
        this.nextHandler = nextHandler;
        return this.nextHandler;
    }
}
