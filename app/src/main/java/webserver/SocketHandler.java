package webserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class SocketHandler {
    // 클라이언트 연결 받을 서버 소켓 생성
    private ServerSocket welcomeSocket;
    // 서버에서 허용할 최대 연결 수 저장
    private Integer maxConnection = 0;
    //private static Integer currentConnection = 0;
    // threadPool 사용하여 멀티 스레딩 환경 관리
    private ExecutorService threadPool;

    // 생성자 : 서버 소켓을 초기화하고 maxConnection 설정
    public SocketHandler(Integer port, Integer maxConnection) throws IOException{
        welcomeSocket = new ServerSocket(port);
        threadPool = Executors.newFixedThreadPool(maxConnection);
        this.maxConnection = maxConnection;
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
        if (((ThreadPoolExecutor) this.threadPool).getActiveCount() >= this.maxConnection){
            // maxConnection 초과 시 503 에러 메시지
            sendUnavailable(connectionSocket);
            return;
        }

        threadPool.execute(()->{
            try {
                Server server = new Server(connectionSocket);
                server.serve();
            } catch (IOException e) {
                System.err.println("serve에 실패했습니다.");
            } finally {
                try{
                    connectionSocket.close();
                } catch (IOException e){
                    System.err.println("소켓을 닫는 중 오류가 발생");
                }
            }

        });

        /*
        // 새로운 스레드 사용하여 클라이언트 요청 처리
        threadPool.execute(() -> {
            System.out.println("클라이언트 요청 처리 중");
            Server server = new Server(connectionSocket);
            server.serve();
            System.out.println("클라이언트 요청 처리 완료");
        });*/
    }

    // maxConnenction 초과 시 클라이언트에 503 에러 메시지 전송
    private void sendUnavailable(Socket connectionSocket) {
        try {
            // 클라이언트에게 메시지 보내기 위해 만드는 출력 도구
            PrintWriter writer = new PrintWriter(connectionSocket.getOutputStream(), true);
            
            // HTTP 응답 헤더 작성 후 클라이언트에게 전송
            writer.print("HTTP/1.1 503 Service Unavailable\r\n");
            writer.print("Connection: close\r\n");
            writer.print("Content-Length: 0\r\n");
            writer.println();
            writer.flush(); // 버퍼에 저장된 데이터를 즉시 출력하도록 강제

            // writer.println("HTTP/1.1 503 Service Unavailable");
            // writer.println("Date: " + new java.util.Date());
            // writer.println("Connection: close");
            // writer.println("Content-Length: 0");
            // writer.println();  // 빈 줄 추가로 헤더와 본문을 구분
            // writer.flush();    // 버퍼에 저장된 데이터 즉시 전송
            
            // 메시지 전송 후 클라이언트 소켓 close
            connectionSocket.close();
        }
        catch (IOException e) {
            System.err.println("503 응답을 보내는 동안 오류 발생" + e.getMessage());
        }
    }

    // private void sendAvailable(Socket connectionSocket, HttpResponse response) {
    //     try {
    //         PrintWriter writer = new PrintWriter(connectionSocket.getOutputStream(), true);
            
    //         // HTTP 응답 헤더 작성 후 클라이언트에게 전송
    //         writer.println(response.rawData);
    //         writer.println();
    //         writer.flush(); // 버퍼에 저장된 데이터를 즉시 출력하도록 강제
            
    //         // 메시지 전송 후 클라이언트 소켓 close
    //         //connectionSocket.close();
    //     }
    //     catch (IOException e) {
    //         System.err.println("응답을 보내는 동안 오류 발생" + e.getMessage());
    //     }
    // }

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