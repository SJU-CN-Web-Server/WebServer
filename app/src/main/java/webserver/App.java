/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package webserver;

import java.io.IOException;

public class App {
    public static void main(String[] args) {
        try {
            SocketHandler socketHandler = new SocketHandler(8080, 10);    
            socketHandler.startServer();
        } catch (IOException e) {
            System.err.println("서버소켓을 생성하는 중 오류가 발생했습니다.");
        }
    }
}