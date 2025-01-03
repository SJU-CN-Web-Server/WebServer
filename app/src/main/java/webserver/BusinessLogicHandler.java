package webserver;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

import webserver.data.HttpRequest;
import webserver.data.HttpResponse;

public class BusinessLogicHandler extends HttpHandler {
    private String currentResourcePath;
    private Object resourceData;
    private File resource;

    @Override
    public void process(HttpRequest request, HttpResponse response, Socket connectionSocket) {
        if(isgoToResponse()){
            return;
        }
        // 요청된 리소스 경로를 가져옴
        currentResourcePath = request.absPath;  // 사용자가 요청한 파일의 경로를 가져옴
        currentResourcePath = URLDecoder.decode(currentResourcePath, StandardCharsets.UTF_8);

        // 멤버 변수인 resource 설정
        resource = new File(currentResourcePath);   

        boolean exists = checkResourceExists(); // 이 파일이 실제로 있는지 확인

        if (exists) {
            try {
                // 리소스가 존재하면 데이터를 읽어옴
                resourceData = readResource(request); // 파일이나 폴더를 읽음
                response.status = 200; // 상태 코드 200 설정 (OK)
                response.body = resourceData.toString(); // 파일 내용이나 폴더 목록을 응답 메시지에 저장
            } catch (IOException e) {
                // 파일 읽기 중 오류 발생 시 상태 코드 500 설정
                setErrorResponse(response, 500);
            }
        } else {
            // 리소스가 존재하지 않으면 상태 코드 404 설정
            setErrorResponse(response, 404);
        }
    }

    // 리소스의 존재 여부를 확인하는 메서드
    private boolean checkResourceExists() {
        return resource.exists();
    }

    // 지정된 리소스를 읽어 데이터를 반환하는 메서드
    private String readResource(HttpRequest request) throws IOException {
        if (resource.isDirectory()) {
            request.isDirectory = true;

            File[] listFiles = resource.listFiles();
            String listFilesString = "";
            String directoryPostfix;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH시 mm분 ss초");

            if (listFiles == null || listFiles.length == 0) {
                return ""; // 빈 디렉토리의 경우 빈 문자열 반환
            }
            
            for (File file : listFiles) {
                directoryPostfix = file.isDirectory() ? "/" : "";
                listFilesString += (file.getName() + directoryPostfix + ":" + file.length() + "bytes:" + dateFormat.format(new Date(file.lastModified())) + "!");
            }

            return listFilesString; // 디렉토리의 경우 파일 목록 반환
        } 
        else {
            request.isDirectory = false;
            return new String(Files.readAllBytes(resource.toPath())); // 파일의 경우 내용 반환
        }
    }

    // 응답 객체에 상태 코드와 오류 메시지를 설정하는 메서드
    private void setErrorResponse(HttpResponse response, int code) {
        response.status = code;
    }
}