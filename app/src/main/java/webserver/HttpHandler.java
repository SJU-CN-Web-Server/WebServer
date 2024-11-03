package webserver;


import java.net.Socket;

import webserver.data.HttpRequest;
import webserver.data.HttpResponse;

public abstract class HttpHandler {
    private HttpHandler nextHandler;
    private boolean goToResponse = false;
    
    public abstract void process(HttpRequest request, HttpResponse response, Socket connectionSocket);

    public boolean handle(HttpRequest request, HttpResponse response, Socket connectionSocket){

        process(request, response, connectionSocket);
        //goToResponse해야 할 때에는 false 반환
        if(goToResponse){
            return false;
        }

        //아니라면 다음 handler 실행
        if(!goToResponse && nextHandler != null){
            nextHandler.handle(request, response, connectionSocket);
        }

        return true;
    }

    public HttpHandler setNextHandler(HttpHandler nextHandler) {
        this.nextHandler = nextHandler;
        return this.nextHandler;
    }

    public void setGoToResponse(boolean goToResponse) {
        this.goToResponse = goToResponse;
    }
}
