package webserver;

import java.io.*;
import java.net.*;
import webserver.data.*;

public class ResponseHandler extends HttpHandler{
    @Override
    public void process(HttpRequest request, HttpResponse response, Socket connectionSocket) {
        try {
            PrintWriter message = new PrintWriter(connectionSocket.getOutputStream(), true);
            
            message.println("HTTP/"+response.version+" "+response.status+"\r\n");
            message.println("Date: "/*+서버 날짜 추가요망*/+"\r\n");
            message.println("Server: "/*+서버 정보 추가요망*/+"\r\n");
            if (response.status.equalsIgnoreCase("200 OK"))
                message.println("Last-Modified: "/*+파일 정보*/+"\r\n");
            if (response.contentLength!=null)
                message.println("Content-Length: "+response.contentLength+"\r\n");
            message.println("Connection: "+response.connection+"\r\n");
            if (response.keepAlive!=null)
                message.println("Keep-Alive: "+response.keepAlive+"\r\n");
            if (response.contentType!=null)
                message.println("Content-Type: "+response.contentType+"\r\n");
            

        } catch (Exception e) {
            e.printStackTrace();
        }   
    }
}