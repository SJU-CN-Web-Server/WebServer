package webserver;

import java.io.*;
import java.net.*;
import webserver.data.*;

public class ResponseHandler extends HttpHandler{
    @Override
    public void process(HttpRequest request, HttpResponse response, Socket connectionSocket) {
        try {
            PrintWriter message = new PrintWriter(connectionSocket.getOutputStream(), true);

            
        } catch (Exception e) {
            e.printStackTrace();
        }   
    }
}