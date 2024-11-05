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
/*
    public void serve(){
        do{
            initializeRequestResponse();
            if(getRequest()){ //에러발생하는 부분
                // System.out.println("requestString: "+httpRequest.rawData);
                entryHandler.handle(httpRequest, httpResponse, connectionSocket);
                sendAvailable(connectionSocket, httpResponse);
            }
        } while(isConnectionAlive());
        closeSocket();
    }
*/
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
            //SocketHandler.decreaseConnection();
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
        //} catch(SocketTimeoutException e){
        //    System.err.println("Socket timed out: " + e.getMessage());
        } 
        catch (IOException e) {
            System.err.println("Error Occured: "+e.getMessage());
            closeSocket();
        }
                
        // Boolean flag = false;
        // try{
        //     InputStream in = connectionSocket.getInputStream();
        //     ByteArrayOutputStream out = new ByteArrayOutputStream();
        //     byte[] buffer = new byte[1024];
        //     int length;
        //     while((length = in.read(buffer)) != -1){
        //         flag = true;
        //         out.write(buffer, 0, length);
        //         String currentData = out.toString("UTF-8");

        //         // // 요청이 끝났는지 확인: 빈 줄 "\r\n\r\n"이 있으면 완료로 판단
        //         if (currentData.contains("\r\n\r\n")) {
        //             // requestComplete = true;
        //             break;
        //         }
        //     }
        //     httpRequest.rawData = out.toString();
        //     // logger.info(() -> "요청 받음: " + httpRequest.rawData);
        //     return flag;
        // }
        // catch(IOException e){
        //     logger.warning(() -> "클라이언트 요청을 받는 동안 오류 발생" + e.getMessage());
        // }
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
            // System.out.println(response.rawData);

            byte[] responseBytes = response.rawData.getBytes();
            byte[] newlineBytes = "\r\n".getBytes();
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
            closeSocket();
        }
    }

    
    /* GET, HEAD 이외의 메서드를 처리 테스트용
    private boolean getRequestForTest() {
        StringBuilder requestBuilder = new StringBuilder();
        
        requestBuilder.append("PUT / HTTP/1.1\r\n");
        requestBuilder.append("Host: 127.0.0.1\r\n");
        requestBuilder.append("Content-Type: application/json\r\n");
        requestBuilder.append("Content-Length: 82\r\n");
        requestBuilder.append("\r\n");
        requestBuilder.append("{");
        requestBuilder.append("\"name\": \"John Doe\",");
        requestBuilder.append("\"email\": \"john.doe@example.com\",");
        requestBuilder.append("\"role\": \"admin\"");
        requestBuilder.append("}\r\n");
    
        // 작성한 요청 메시지를 httpRequest 객체에 할당
        httpRequest.rawData = requestBuilder.toString();
        
        // 플래그 반환 (true는 요청이 정상적으로 작성되었음을 의미)
        return true;
    }
    */
}