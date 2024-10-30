package webserver;
import java.net.*;

public class KeepAliveHandler{
    private int maxRequests;
    private int requestCount;
    private boolean keepAlive;
    private int timeout;

    public KeepAliveHandler() {
        maxRequests=100;
        requestCount=0;
        keepAlive=true;
        timeout=5000;
    }

    void setTimeout(Socket clientSocket){
        
    }

    void increaseResquestCount(){
        requestCount++;
    }

    boolean isClosed() {
        return (!keepAlive||requestCount >= maxRequests);
    }
}