package webserver;

import webserver.data.HttpRequest;
import webserver.data.HttpResponse;

public class ResponseHandler extends HttpHandler{
    @Override
    public void process(HttpRequest request, HttpResponse response) {
        String responseMessage = convertToHttpMessage(response);
        System.out.println(responseMessage);
        //socket.write(responseMessage); //psudo code
    }

    public static String convertToHttpMessage(HttpResponse response) {
        StringBuilder httpMessage = new StringBuilder();

        // 상태 줄(Status-Line) 생성
        httpMessage.append(response.version != null ? response.version : "HTTP/1.1")
                   .append(" ")
                   .append(response.status != null ? response.status : 200)
                   .append(" ")
                   .append(getReasonPhrase(response.status != null ? response.status : 200))
                   .append("\r\n");

        // 헤더 생성 (각 속성이 null이 아닌 경우에만 추가)
        if (response.contentType != null) {
            httpMessage.append("Content-Type: ").append(response.contentType).append("\r\n");
        }
        if (response.contentLength != null) {
            httpMessage.append("Content-Length: ").append(response.contentLength).append("\r\n");
        }
        if (response.KeepAlive != null) {
            httpMessage.append("Keep-Alive: ").append(response.KeepAlive).append("\r\n");
        }
        if (response.Connection != null) {
            httpMessage.append("Connection: ").append(response.Connection).append("\r\n");
        }
        if (response.cache_control != null) {
            httpMessage.append("Cache-Control: ").append(response.cache_control).append("\r\n");
        }
        if (response.cache_expires != null) {
            httpMessage.append("Expires: ").append(response.cache_expires).append("\r\n");
        }

        // 빈 줄을 추가하여 헤더와 본문 구분
        httpMessage.append("\r\n");

        // 본문 추가
        if (response.body != null) {
            httpMessage.append(response.body);
        }

        return httpMessage.toString();
    }

    // 상태 코드에 따른 설명 문구
    private static String getReasonPhrase(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 304 -> "Not Modified";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Unknown Status";
        };
    }
}


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