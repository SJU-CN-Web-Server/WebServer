package webserver;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
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
        do{
            initializeRequestResponse();
            if(getRequest()){ //에러발생하는 부분
                entryHandler.handle(httpRequest, httpResponse, connectionSocket);
                sendAvailable(connectionSocket, httpResponse);
                System.out.println("hmm");
            }
        } while(isConnectionAlive());
        closeSocket();
    }

    private void closeSocket() {
        try {
            connectionSocket.close();
            SocketHandler.decreaseConnection();
        } catch (Exception e) {
            System.err.println("소켓 닫는 중 오류 발생" + e.getMessage());
        }
    }

    private boolean getRequest(){
        Boolean flag = false;
        try{
            InputStream in = connectionSocket.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while((length = in.read(buffer)) != -1){
                flag = true;
                out.write(buffer, 0, length);
                String currentData = out.toString("UTF-8");

                // // 요청이 끝났는지 확인: 빈 줄 "\r\n\r\n"이 있으면 완료로 판단
                if (currentData.contains("\r\n\r\n")) {
                    // requestComplete = true;
                    break;
                }
            }
            httpRequest.rawData = out.toString();
            // logger.info(() -> "요청 받음: " + httpRequest.rawData);
            return flag;
        }
        catch(IOException e){
            logger.warning(() -> "클라이언트 요청을 받는 동안 오류 발생" + e.getMessage());
        }
        return false;
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
            DataOutputStream out = new DataOutputStream(connectionSocket.getOutputStream());
            byte[] responseBytes = response.rawData.getBytes();
            byte[] newlineBytes = "\r".getBytes();
            byte[] combinedBytes = new byte[responseBytes.length + newlineBytes.length];
            System.arraycopy(responseBytes, 0, combinedBytes, 0, responseBytes.length);
            System.arraycopy(newlineBytes, 0, combinedBytes, responseBytes.length, newlineBytes.length);
            out.write(combinedBytes);
            out.flush();
            // PrintWriter writer = new PrintWriter(connectionSocket.getOutputStream(), true);
            
            // // HTTP 응답 헤더 작성 후 클라이언트에게 전송
            // // System.out.println("final"+response.rawData);
            // writer.println(response.rawData);
            // //writer.println();
            // writer.flush(); // 버퍼에 저장된 데이터를 즉시 출력하도록 강제
            // writer.close();
            
            // 메시지 전송 후 클라이언트 소켓 close
            //connectionSocket.close();
        }
        catch (IOException e) {
            System.err.println("응답을 보내는 동안 오류 발생" + e.getMessage());
        }
    }}
