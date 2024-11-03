package webserver;

import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import webserver.data.HttpRequest;
import webserver.data.HttpResponse;

public class RouterHandler extends HttpHandler {
    private final List<Map<String, String>> mappings = new ArrayList<>();

    // 매핑을 설정하는 생성자 (예제에서 하드코딩, 외부 설정 파일을 읽는 경우로도 변경 가능)
    public RouterHandler() {
        // URL 패턴과 파일 시스템 경로 매핑을 하드코딩 예제로 설정
        mappings.add(Map.of("urlPattern", "/tmp/*", "fileSystemPath", "/var/www/html/tmp"));
        mappings.add(Map.of("urlPattern", "/docs/*", "fileSystemPath", "/var/www/html/docs"));
    }

    @Override
    public void process(HttpRequest request, HttpResponse response, Socket clientSocket) {
        request.absPath = getAbsPath(request);
        System.out.println("absPath: " + request.absPath);
    }

    String getAbsPath(HttpRequest request) {
        String basePath = getFileSystemPath(request.path);  // 매핑된 파일 시스템 경로 가져오기
        String relativePath = stripPatternFromPath(request.path, getUrlPattern(request.path));
        Path path = Paths.get(basePath, relativePath).toAbsolutePath();
        return path.toString();
    }
    
    // URL 패턴 부분을 제거하여 남은 경로만 반환
    private String stripPatternFromPath(String urlPath, String urlPattern) {
        if (urlPattern.endsWith("/*")) {
            String basePattern = urlPattern.replace("/*", "");
            if (urlPath.startsWith(basePattern)) {
                return urlPath.substring(basePattern.length());
            }
        }
        return "";  // 패턴이 없는 경우 빈 문자열 반환
    }
    
    // URL 경로에 해당하는 URL 패턴을 찾는 메서드
    private String getUrlPattern(String urlPath) {
        for (Map<String, String> mapping : mappings) {
            String urlPattern = mapping.get("urlPattern");
            if (urlPath.matches(urlPattern.replace("*", ".*"))) {
                return urlPattern;
            }
        }
        return "";  // 매칭되는 패턴이 없을 경우 빈 문자열 반환
    }


    // URL 경로에 맞는 파일 시스템 경로를 반환
    private String getFileSystemPath(String urlPath) {
        for (Map<String, String> mapping : mappings) {
            String urlPattern = mapping.get("urlPattern");
            String fileSystemPath = mapping.get("fileSystemPath");

            // urlPattern의 '*'을 정규식 '.*'로 변환하여 매칭 검사
            if (urlPath.matches(urlPattern.replace("*", ".*"))) {
                return fileSystemPath;
            }
        }
        return "/var/www/html";  // 기본 경로 반환 (필요시 수정 가능)
    }

}
