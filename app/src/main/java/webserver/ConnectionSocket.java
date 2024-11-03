//작성 중.
package webserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Optional;
import java.util.logging.Logger;

public class ConnectionSocket {
    private static final Logger logger = Logger.getLogger(ConnectionSocket.class.getName());

    private Socket socket = null;
    public ConnectionSocket(Socket connectionSocket){
        this.socket = connectionSocket;
    }

    public boolean closeSocket(){
        try{
            socket.close();
            return true;
        }
        catch(IOException e){
            logger.warning(() -> "소켓 닫는 중 오류 발생" + e.getMessage());
            return false;
        }
    }

    public Optional<String> getMessages(){
        try{
            InputStream in = socket.getInputStream();
            ByteArrayOutputStream message = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while((length = in.read(buffer)) != -1){
                message.write(buffer, 0, length);
                if(length < 1024){
                    break;
                }
            }
            return Optional.of(message.toString());
        }
        catch(IOException e){
            logger.warning(() -> "클라이언트 요청을 받는 동안 오류 발생" + e.getMessage());
        }
        return Optional.empty();
    }

}
