package webserver;

import java.io.*;
import java.net.*;
import webserver.data.*;

public class ResponseGenerator extends HttpHandler{
    @Override
    public void process(HttpRequest request, HttpResponse response, Socket clienSocket) {
        try {
            PrintWriter message = new PrintWriter(clienSocket.getOutputStream(), true);    
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        //if (response.version!=null)
            
    }
}