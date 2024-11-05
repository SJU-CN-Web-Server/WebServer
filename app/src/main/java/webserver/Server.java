package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

import webserver.data.HttpRequest;
import webserver.data.HttpResponse;

public final class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private final Socket connectionSocket;
    // 기본 타임아웃 설정 5초
    private static final int SOCKET_TIMEOUT = 5000;

    HttpRequest httpRequest;
    HttpResponse httpResponse;

    HttpHandler entryHandler;

    HttpRequestParser httpRequestParser;
    RouterHandler httpRequestRouter;
    HttpCachingHandler cacheHandler;
    BusinessLogicHandler businessLogicHandler;
    ResponseBodyCreator responseBodyCreator;

    KeepAliveHandler keepAliveHandler;
    ResponseHandler responseHandler;

    public Server(Socket connectionSocket) throws SocketException{
        this.connectionSocket = connectionSocket;
        //커넥션 소켓 생성시 즉시 타임아웃 설정
        this.connectionSocket.setSoTimeout(SOCKET_TIMEOUT);
        logger.info(() -> "\nConnection Socket ID: " + connectionSocket.hashCode());
        initializeHandler();
        initializeHandlerChain();
    }

    public void serve() throws IOException {
        try {
            initializeRequestResponse();
            if (getRequest()){
                entryHandler.handle(httpRequest, httpResponse, connectionSocket);
                sendAvailable(connectionSocket, httpResponse);
            }    
        } catch (SocketTimeoutException e) {
            System.out.println("timeout! 연결을 종료합니다.");
            closeSocket();
        } catch (Exception e){
            System.out.println("요청 처리 중 에러 발생"+e.getMessage());
            closeSocket();
        }
        
    }

    private void closeSocket() {
        try {
            connectionSocket.close();
        } catch (Exception e) {
            System.err.println("소켓 닫는 중 오류 발생" + e.getMessage());
        }
    }

    private boolean getRequest() throws SocketTimeoutException {
        String requestString = null;
        StringBuilder requestBuilder = new StringBuilder();
        String line;
        Boolean flag = false;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            int contentLength = 0;
            // 헤더 부분 읽기
            while (true) {
                line = in.readLine();
                if (line == null || line.isBlank()) break;
                flag = true;
                requestBuilder.append(line).append("\r\n");
                // Content-Length 헤더를 통해 body의 길이를 가져옴
                if (line.startsWith("Content-Length")) {
                    contentLength = Integer.parseInt(line.split(": ")[1]);
                }
            }

            // 빈 줄 추가 (header와 body를 구분하는 빈 줄)
            requestBuilder.append("\r\n");

            // body 읽기
            if (contentLength > 0) {
                char[] body = new char[contentLength];
                in.read(body, 0, contentLength);
                requestBuilder.append(body);
            }
            
            requestString = requestBuilder.toString();
            httpRequest.rawData = requestString;
            return flag;
        } 
        catch (IOException e) {
            System.err.println("Error Occured: "+e.getMessage());
            closeSocket();
        }
        
        return false;
    }

    public boolean isConnectionAlive() {
        return keepAliveHandler.isKeepAlive() && !connectionSocket.isClosed() && connectionSocket.isConnected();
    }

    private void initializeRequestResponse() {
        httpRequest = new HttpRequest();
        httpResponse = new HttpResponse();
    }
    
    private void initializeHandler() {
        httpRequestParser = new HttpRequestParser();
        httpRequestRouter = new RouterHandler();
        cacheHandler = new HttpCachingHandler();
        businessLogicHandler = new BusinessLogicHandler();
        responseBodyCreator = new ResponseBodyCreator();
        keepAliveHandler = new KeepAliveHandler();
        responseHandler = new ResponseHandler();
        entryHandler = httpRequestParser;
    }

    private void initializeHandlerChain() {
        httpRequestParser.setNextHandler(httpRequestRouter)
                .setNextHandler(cacheHandler)
                .setNextHandler(businessLogicHandler)
                .setNextHandler(responseBodyCreator)
                .setNextHandler(keepAliveHandler)
                .setNextHandler(responseHandler);
    }

    private void sendAvailable(Socket connectionSocket, HttpResponse response) {
        try {
            DataOutputStream out = new DataOutputStream(connectionSocket.getOutputStream());

            byte[] responseBytes = response.rawData.getBytes();
            byte[] newlineBytes = "\r\n".getBytes();
            byte[] combinedBytes = new byte[responseBytes.length + newlineBytes.length];
            System.arraycopy(responseBytes, 0, combinedBytes, 0, responseBytes.length);
            System.arraycopy(newlineBytes, 0, combinedBytes, responseBytes.length, newlineBytes.length);
            out.write(combinedBytes);
            out.flush();
        }
        catch (IOException e) {
            System.err.println("응답을 보내는 동안 오류 발생" + e.getMessage());
            closeSocket();
        }
    }
}