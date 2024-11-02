//서현 코드
package webserver;

import java.net.Socket;

import webserver.data.HttpRequest;
import webserver.data.HttpResponse;

public class ResponseBodyCreator extends HttpHandler {
		
    // 비즈니스 로직으로부터 리소스를 받아 응답을 생성하는 메소드(응답의 본문 생성)
    // HttpRequest와 HttpResponse 객체를 사용하여 응답을 생성
    @Override
    public void process(HttpRequest request, HttpResponse response, Socket clientSocket) {
        StringBuilder responseBody = new StringBuilder("<html><body>"); 

        if (response.status == 200) { // 상태 코드가 200이면 성공적인 응답이므로 본문 생성
            
            if (request.userAgent != null) {
                String lowerUserAgent = request.userAgent.toLowerCase();
                responseBody.append("<h2>Welcome, ");
              
                if (lowerUserAgent.contains("mobile")) {
                    responseBody.append("Mobile User!");
                } else if (lowerUserAgent.contains("chrome")) {
                    responseBody.append("Chrome Browser User!");
                } else if (lowerUserAgent.contains("firefox")) {
                    responseBody.append("Firefox User!");
                } else {
                    responseBody.append("Web User!");
                }
                responseBody.append("</h2>");
            }

            // Accept-Language에 따른 언어 맞춤화 추가
            if (request.acceptLanguage != null && request.acceptLanguage.toLowerCase().contains("ko")) {
                responseBody.append("<p>안녕하세요! 요청하신 데이터는 다음과 같습니다:</p>");
            } else {
                responseBody.append("<p>Hello! Here is the data you requested:</p>");
            }

            // 요청된 리소스가 디렉토리인지 파일인지 확인하여 응답 본문에 포함
            if (response.body != null) {
                if (response.body.startsWith("[")) { // 디렉토리로부터 파일 목록일 경우
                    responseBody.append(createDirectoryResponse(response.body));
                } else { // 파일의 경우
                    responseBody.append("<p>").append(response.body).append("</p>");
                }
            } else {
		            // response.status = 404;
                // responseBody.append(createErrorResponse(response.status));
            }
        } else { // 상태 코드가 200이 아니면 오류 응답을 생성
            responseBody.append(createErrorResponse(response.status));
        }

        responseBody.append("</body></html>");

        // 응답 객체에 본문 및 기타 정보 설정
        response.body = responseBody.toString();
        response.contentType = "text/html";
        // response.contentLength = String.valueOf(response.body.length()); // 불필요함. response.contentLength가 이미 Integer임.
        response.contentLength = response.body.length();
    }

    // 오류 응답을 생성하는 메소드
    private String createErrorResponse(int statusCode) {
        return "<h1>" + statusCode + " " + getStatusMessage(statusCode) + "</h1>";
    }
  
    // 상태 코드에 따른 상태 메시지 반환
    private String getStatusMessage(int statusCode) {
        switch (statusCode) {
            // case 200: return "OK";
            case 404: return "Not Found";
            case 304: return "Not Modified";
            default: return "Internal Server Error";
        }
    }

    // 디렉토리 응답을 생성하는 메소드
    private String createDirectoryResponse(String directoryContents) {
        StringBuilder response = new StringBuilder("<h1>Directory Contents</h1><ul>");
        String[] files = directoryContents.replace("[", "").replace("]", "").split(", ");
        for (String file : files) {
            response.append("<li>").append("<a href='/" + file + "'>").append(file).append("</a></li>");
        }
        response.append("</ul>");
        return response.toString();
    }
}
