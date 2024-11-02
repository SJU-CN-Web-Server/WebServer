package webserver;

import webserver.data.HttpRequest;
import webserver.data.HttpResponse;

public abstract class HttpHandler {
    private HttpHandler nextHandler;
    private boolean goToResponse = false;
    
    public abstract void process(HttpRequest request, HttpResponse response);

    public void handle(HttpRequest request, HttpResponse response){
        process(request, response);
        //추가 구현예정
        if(goToResponse){

            return;
        }
        if(!goToResponse && nextHandler != null){
            nextHandler.handle(request, response);
        }
    }

    public HttpHandler setNextHandler(HttpHandler nextHandler) {
        this.nextHandler = nextHandler;
        return this.nextHandler;
    }

    public void setGoToResponse(boolean goToResponse) {
        this.goToResponse = goToResponse;
    }
}
