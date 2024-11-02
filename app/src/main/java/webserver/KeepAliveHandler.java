package webserver;
//import java.net.*;

import webserver.data.*;;

public class KeepAliveHandler extends HttpHandler{
    private int maxRequests=100;
    private int requestCount=0;
    private boolean keepAlive=true;
    private int timeout=5000;

    /*
    void setTimeout(Socket clientSocket){
        try{
            clientSocket.setSoTimeout(timeout);
        } catch(SocketException e){

        }
    }
    */

    @Override
    public void process(HttpRequest request, HttpResponse response){
        requestCount++;
        if (request.connection.equalsIgnoreCase("close")||requestCount>=maxRequests){
            response.connection="close";
            response.keepAlive=null;
            keepAlive=false;
        }
        else {
            if (request.keepAlive!=null) {
                String[] token = response.keepAlive.split(",");
                int newTimeout = Integer.parseInt(token[0].trim().split("=")[1]);
                int newMaxRequests = Integer.parseInt(token[1].trim().split("=")[1]);
                timeout = newTimeout < 10 ? newTimeout*1000:10000;
                maxRequests = newMaxRequests < 100 ? newMaxRequests:100;
            }
            response.connection="Keep-Alive";
            response.keepAlive="timeout="+Integer.toString(timeout)+ "max="+Integer.toString(maxRequests);
            keepAlive=true;
        }
    }
}