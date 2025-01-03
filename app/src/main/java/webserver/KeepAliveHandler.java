package webserver;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;

import webserver.data.HttpRequest;
import webserver.data.HttpResponse;
;

public class KeepAliveHandler extends HttpHandler{
    // 한번의 TCP연결에서 최대로 요청 받을 수 있는 메세지 수
    // 서버 기본 설정 100
    private int maxRequests=100;
    // 현재 요청 된 메세지 수
    private int requestCount=0;
    // keep-alive 사용 여부
    private boolean keepAlive=true;
    // TTL 서버 기본 설정 5초
    private int timeout=5000;
    private HttpResponse response = null;

    // 요청 된 메세지 수가 최대 메세지 수 보다 크다면 return true; -> socket close 조건
    public boolean isRequsetLimitExceeded(){
        return requestCount >= maxRequests;
    }
    // return keep-alive 사용여부; socket close 조건
    public boolean isKeepAlive(){
        if(response != null
        && response._date != null
        && (new Date()).after(new Date(response._date.getTime() + timeout))){
            System.out.println("timeout");
            keepAlive=false;
        }
        return keepAlive;
    }

    @Override
    public void process(HttpRequest request, HttpResponse response, Socket connectionSocket) {
        this.response = response;

        requestCount++;
        // request 메세지의 Connection: 헤더가 close이거나 요청된 메세지의 수가 최대 메세지 수 보다 크다면 keep-alive를 close로 설정
        Boolean connection = request.connection.map(conn -> conn.equalsIgnoreCase("close")).orElse(false);
        if (Boolean.TRUE.equals(connection) || isRequsetLimitExceeded()){
            response.connection="close";
            response.keepAlive=null;
            keepAlive=false;
        }
        // keep-alive 설정
        else {
            // request 메세지에 keep-alive 설정이 있을 경우 제약 조건에 맞게 설정
            if (request.keepAlive!=null) {
                String[] token = response.keepAlive.split(",");
                int newTimeout = Integer.parseInt(token[0].trim().split("=")[1]);
                int newMaxRequests = Integer.parseInt(token[1].trim().split("=")[1]);
                // TTL 설정, 허용 최대 시간 10초
                timeout = newTimeout < 10 ? newTimeout*1000:10000;
                // 최대 요청 메세지 수 설정, 허용 최대 값 100
                maxRequests = newMaxRequests < 100 ? newMaxRequests:100;
            }
            // request 메세지에 keep-alive 설정이 없을 경우 default로 설정
            response.connection="Keep-Alive";
            response.keepAlive="timeout="+Integer.toString(timeout/1000)+ ", max="+Integer.toString(maxRequests);
            keepAlive=true;
        }
        // 소켓 TTL 설정
        try {
            connectionSocket.setSoTimeout(timeout);
        } catch (SocketException e){
            System.err.println("Error Occured:"+e.getMessage());
        }
    }
}