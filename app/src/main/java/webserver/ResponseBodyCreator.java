package webserver;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import webserver.data.HttpRequest;
import webserver.data.HttpResponse;

public class ResponseBodyCreator extends HttpHandler {
		
    // 비즈니스 로직으로부터 리소스를 받아 응답을 생성하는 메소드(응답의 본문 생성)
    // HttpRequest와 HttpResponse 객체를 사용하여 응답을 생성
    @Override
    public void process(HttpRequest request, HttpResponse response, Socket connectionSocket) {
        if(isgoToResponse()){
            return;
        }
        if(request.isDownload) {
            handleFileDownload(request, response);
            return;
        }
        StringBuilder responseBody = new StringBuilder("<html><!DOCTYPE html><head>" + //
                        "<meta charset=\"UTF-8\">" + //
                        "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" + //
                        "<title>" + request.path + "</title>" + //
                        "</head><body>"); 

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
                if (request.isDirectory) { // 디렉토리일 경우
                    responseBody.append(createDirectoryResponse(response.body, request));
                } else { // 파일일 경우
                    responseBody.append("<a href='/download").append(request.path).append("'>파일 다운로드</a>");
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
        // System.out.println("responseBODY:"+response.body);
        response.contentType = "text/html";
        response.contentLength = response.body.getBytes().length+1;

    }

    public void handleFileDownload(HttpRequest request, HttpResponse response) {
        try {
            String filePath = URLDecoder.decode(request.absPath, StandardCharsets.UTF_8);
            File file = new File(filePath);
            
            // Content-Type 결정
            String mimeType = Files.probeContentType(Paths.get(request.absPath));
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            
            // Content-Disposition, Content-Length 등 헤더 작성
            response.contentType = mimeType;
            response.contentDisposition = "attachment; filename=\"" + file.getName() + "\"";
            response.contentLength = (int) file.length();
            
            // 파일 데이터 body에 추가
            response.body = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    private String createDirectoryResponse(String directoryContents, HttpRequest request) {
        StringBuilder response = new StringBuilder("<h1>Directory Contents</h1><table><thead><tr><th>이름</th><th>크기</th><th>최근 수정일</th></tr></thead><tbody>");
        if (directoryContents.isEmpty()) {
            response.append("<tr><td colspan='3'>Nothing</td></tr>");
        } else {
            String[] filesList = directoryContents.split("!");
    
            for (String fileString : filesList) {
                String[] fileInfos = fileString.split(":");
                response.append("<tr>").append("<td><a href='" + request.path + fileInfos[0] + "'>").append(fileInfos[0]).append("</a></td><td>").append(fileInfos[1]).append("</td><td>").append(fileInfos[2]).append("</td></tr>");
            }
        }
        response.append("</tbody></table>");
        return response.toString();
    }
}