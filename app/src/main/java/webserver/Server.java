package webserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

import webserver.data.HttpRequest;
import webserver.data.HttpResponse;

public final class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private final Socket connectionSocket;

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


    public Server(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
        initializeHandler();
        initializeHandlerChain();
    }

    public void serve() {
        initializeRequestResponse();
        do {
            getRequest();
            entryHandler.handle(httpRequest, httpResponse, connectionSocket);
            sendAvailable(connectionSocket, httpResponse);
            System.out.println("\nThread is Run in serve while()\n");
        } while(isConnectionAlive());
        System.out.println("\nThread get out of loop\n");
        closeSocket();
    }

    private void closeSocket() {
        try {
            connectionSocket.close();
            System.out.println("소켓을 정상적으로 닫았습니다.");
        } catch (Exception e) {
            System.err.println("소켓 닫는 중 오류 발생" + e.getMessage());
        }
    }

    private void getRequest(){
        try{
            InputStream in = connectionSocket.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while((length = in.read(buffer)) != -1){
                out.write(buffer, 0, length);
                if(length < 1024){
                    break;
                }
            }
            httpRequest.rawData = out.toString();
            logger.info(() -> "요청 받음: " + httpRequest.rawData);
        } catch(SocketTimeoutException e){
            System.err.println("소켓 타임아웃 발생");
            closeSocket();
        } catch(IOException e){
            System.err.println("클라이언트 요청을 받는 동안 오류 발생" + e.getMessage());
            //closeSocket();
        }
    }

    public boolean isConnectionAlive() {
        return keepAliveHandler.isKeepAlive();
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
            PrintWriter writer = new PrintWriter(connectionSocket.getOutputStream(), true);
            
            // HTTP 응답 헤더 작성 후 클라이언트에게 전송
            writer.println(response.rawData);
            //writer.println();
            writer.flush(); // 버퍼에 저장된 데이터를 즉시 출력하도록 강제
            
            // 메시지 전송 후 클라이언트 소켓 close
            //connectionSocket.close();
        }
        catch (IOException e) {
            System.err.println("응답을 보내는 동안 오류 발생" + e.getMessage());
        }
    }
}