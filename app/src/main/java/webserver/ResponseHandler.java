//충돌부분: 주석처리

package webserver;

import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import webserver.data.HttpRequest;
import webserver.data.HttpResponse;

public class ResponseHandler extends HttpHandler{
    @Override
    public void process(HttpRequest request, HttpResponse response, Socket connectionSocket) {
        String responseMessage = convertToHttpMessage(response);
        response.rawData = responseMessage;
        if(isgoToResponse()) setGoToResponse(false);
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
        if (response.keepAlive != null) {
            httpMessage.append("Keep-Alive: ").append(response.keepAlive).append("\r\n");
        }
        if (response.connection != null) {
            httpMessage.append("Connection: ").append(response.connection).append("\r\n");
        }
        if (response.cache_control != null) {
            httpMessage.append("Cache-Control: ").append(response.cache_control).append("\r\n");
        }
        if (response.cache_expires != null) {
            httpMessage.append("Expires: ").append(response.cache_expires).append("\r\n");
        }

        if(response.last_modified != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT")); // HTTP Date 헤더는 GMT 시간대를 사용
            httpMessage.append("Last-Modified: ").append(response.last_modified).append("\r\n");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT")); // HTTP Date 헤더는 GMT 시간대를 사용

        Date now = new Date();
        response._date = now;
        String dateHeader = dateFormat.format(now);
        response.date = dateHeader;

        httpMessage.append("Date: ").append(response.date).append("\r\n");

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
            case 405 -> "Method Not Allowed";
            case 500 -> "Internal Server Error";
            default -> "Unknown Status";
        };
    }
}