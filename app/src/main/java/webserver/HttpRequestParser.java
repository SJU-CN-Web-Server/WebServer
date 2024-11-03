package webserver;

import java.net.Socket;

import webserver.data.HttpRequest;
import webserver.data.HttpResponse;

public class HttpRequestParser extends HttpHandler {
    private HttpRequest request;

    // 전체 parsing 과정 수행. 최종 Request 반환
    @Override
    public void process(HttpRequest request, HttpResponse response, Socket clientSocket) {
        this.request = request;
        String[] lines = request.rawData.split("\r\n");

        if(lines.length == 0) return;

        parseStartLine(lines[0]);
        parseHeader(lines);
        parseBody();
    }

    // HttpRequest의 첫 줄(method, URL, version) Parsing하여 Request 객체에 추가
    private void parseStartLine(String startLine) {
        String[] startLineInfos = startLine.split(" ");

        if (startLineInfos.length == 3) {
            request.method = startLineInfos[0];
            request.path = startLineInfos[1];
            request.version = startLineInfos[2];
        }
    }

    // Header Parsing하여 Request 객체의 header에 추가 
    private void parseHeader(String[] lines) {
        for (int i = 1; i < lines.length && !lines[i].isEmpty(); i++) {
            String[] header = lines[i].split(": ", 2);

            if (header.length == 2) {
                switch (header[0].toLowerCase()) {
                    case "host" :
                        request.host = header[1];
                        break;
                    case "user-agent" :
                        request.userAgent = header[1];
                        break;
                    case "accept" :
                        request.accept = header[1];
                        break;    
                    case "accept-language" :
                        request.acceptLanguage = header[1];
                        break;
                    case "accept-charset" :
                        request.acceptCharset = header[1];
                        break;
                    case "connection" :
                        request.connection = header[1];
                        break;
                    case "keep-alive" :
                        request.keepAlive = header[1];
                        break;
                    }
                }
            }
        }

    // Request 본문 Parsing하여 Request 객체의 header에 추가
    private void parseBody() {
        int emptyLineIndex = request.rawData.indexOf("\r\n\r\n");

        // indexOf 메소드는 값이 없을 때 -1 반환
        if (emptyLineIndex != -1) {
            // substring 메소드를 이용해서 rawData의 시작위치부터 자름
            request.body = request.rawData.substring(emptyLineIndex + 4);
        }
    }
}