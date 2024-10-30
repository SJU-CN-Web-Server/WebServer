package webserver;

import webserver.data.HttpRequest;

public abstract class HttpHandler {
    private HttpHandler nextHandler;
    private boolean goToResponse = false;
    
    public abstract void process(HttpRequest request);

    public void handle(HttpRequest request){
        process(request);
        //추가 구현예정
        if(goToResponse){
            return;
        }
        if(!goToResponse && nextHandler != null){
            nextHandler.handle(request);
        }
    }

    public HttpHandler setNextHandler(HttpHandler nextHandler) {
        this.nextHandler = nextHandler;
        return this.nextHandler;
    }
}
