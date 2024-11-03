package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import webserver.data.HttpRequest;
import webserver.data.HttpResponse;

public class SocketHandler {
    // 클라이언트 연결 받을 서버 소켓 생성
    private ServerSocket welcomeSocket;
    // threadPool 사용하여 멀티 스레딩 환경 관리
    private ExecutorService threadPool;
    // maxConnection 초과 여부
    private int maxConnection;


    // 생성자 : 서버 소켓을 초기화하고 maxConnection 설정
    public SocketHandler(int port, int maxConnection) {
        this.maxConnection = maxConnection;

        // maxConnection만큼 스레드를 만들어 멀티 스레딩 환경 관리
        this.threadPool = Executors.newFixedThreadPool(maxConnection);

        try {
            //지정된 포트 번호로 서버 소켓 생성
            this.welcomeSocket = new ServerSocket(port);
        } 
        catch (IOException e) {
            System.err.println("서버 소켓 생성하는 동안 오류 발생" + e.getMessage());
        }
    }

    // 서버 시작하고 클라이언트 연결 대기. 클라이언트 연결되면 handleClientRequest() 호출
    public void startServer() {
        System.out.println("서버 시작");

        while(true) {
            try {
                // 클라이언트 연결 요청 기다림
                Socket connectionSocket = welcomeSocket.accept();

                // 클라이언트 연결 처리
                handleClientRequest(connectionSocket);
            }
            catch (IOException e) {
                System.err.println("클라이언트 연결 수락하는 동안 오류 발생" + e.getMessage());
            }
        }
    }

    // maxConnection 확인하고 초과 시 sendUnavailable() 호출. 그렇지 않으면 클라이언트 요청 처리
    private void handleClientRequest(Socket connectionSocket) {
        if (((ThreadPoolExecutor) this.threadPool).getActiveCount() >= this.maxConnection) {
            // maxConnection 초과 시 503 에러 메시지
            sendUnavailable(connectionSocket);
            return;
        }

        // 새로운 스레드 사용하여 클라이언트 요청 처리
        threadPool.execute(() -> {
            System.out.println("클라이언트 요청 처리 중");
 
            HttpRequest request = new HttpRequest();
            HttpResponse response = new HttpResponse();
            
            HttpHandler httpRequestParser = new HttpRequestParser();
            KeepAliveHandler keepAliveHandler = new KeepAliveHandler();
            HttpHandler routerHandler = new RouterHandler();
            HttpHandler httpCachingHandler = new HttpCachingHandler(); 
            HttpHandler businessLogicHandler = new BusinessLogicHandler();
            HttpHandler responseBodyCreator = new ResponseBodyCreator();
            HttpHandler responseHandler = new ResponseHandler();
                
            // Handler 연결
            httpRequestParser
                .setNextHandler(keepAliveHandler)
                .setNextHandler(routerHandler)
                .setNextHandler(httpCachingHandler)
                .setNextHandler(businessLogicHandler)
                .setNextHandler(responseBodyCreator)
                .setNextHandler(responseHandler);

            //request 메세지 처리
            while(true){
                try {
                    String requestString = null;
                    BufferedReader in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    StringBuilder requestBuilder = new StringBuilder();
                    String line;

                    int contentLength = 0;

                    // 헤더 부분 읽기
                    while (true) {
                        line = in.readLine();
                        if (line == null || line.isBlank()) break;
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
                    request.rawData = requestString;
                    
                    // goToResponse 해야 하면 responseHandler로 바로 연결
                    if(!httpRequestParser.handle(request, response, connectionSocket)){
                        responseHandler.process(request, response, connectionSocket);
                    }

                    sendAvailable(connectionSocket, response);

                    //keepAlive 검사
                    if (keepAliveHandler.isKeepAlive()) break;
                } catch(SocketTimeoutException e){
                    System.err.println("Socket timed out: " + e.getMessage());
                    break;
                } catch (IOException e) {
                    System.err.println("Error Occured: "+e.getMessage());
                }
            }

            try {
                connectionSocket.close();
            } catch(IOException es){
                System.err.println("Failed to close connection socket: "+es.getMessage());
            }    
        });
    }

    // maxConnenction 초과 시 클라이언트에 503 에러 메시지 전송
    private void sendUnavailable(Socket connectionSocket) {
        try {
            // 클라이언트에게 메시지 보내기 위해 만드는 출력 도구
            PrintWriter writer = new PrintWriter(connectionSocket.getOutputStream(), true);
            
            // HTTP 응답 헤더 작성 후 클라이언트에게 전송
            writer.println("HTTP/1.1 503 Service Unavailable\r\n");
            writer.println("Connection: close\r\n");
            writer.println();
            writer.flush(); // 버퍼에 저장된 데이터를 즉시 출력하도록 강제
            
            // 메시지 전송 후 클라이언트 소켓 close
            //connectionSocket.close();
        }
        catch (IOException e) {
            System.err.println("503 응답을 보내는 동안 오류 발생" + e.getMessage());
        }
    }

    private void sendAvailable(Socket connectionSocket, HttpResponse response) {
        try {
            PrintWriter writer = new PrintWriter(connectionSocket.getOutputStream(), true);
            
            // HTTP 응답 헤더 작성 후 클라이언트에게 전송
            writer.println(response.rawData);
            writer.println();
            writer.flush(); // 버퍼에 저장된 데이터를 즉시 출력하도록 강제
            
            // 메시지 전송 후 클라이언트 소켓 close
            //connectionSocket.close();
        }
        catch (IOException e) {
            System.err.println("응답을 보내는 동안 오류 발생" + e.getMessage());
        }
    }

    // 서버 종료하고 열려 있는 모든 소켓을 close
    public void closeServer() {
        try {
            // 서버 소켓 close
            if (welcomeSocket != null & !welcomeSocket.isClosed()) {
                welcomeSocket.close();
                // 요청을 처리하지 못하고 서버가 종료되거나
                // 스레드 풀 내에서 작업이 끝나기 전에 closeServer()가 호출되었거나
                // 클라이언트 측에서 정상적으로 연결이 종료하지 않은 경우
            }

            // 스레드 종료
            if (threadPool != null) {
                threadPool.shutdown();
            }

            System.out.println("서버 종료");
        }
        catch (IOException e) {
            System.err.println("서버를 종료하는 동안 오류 발생" + e.getMessage());
        }
    }
}
