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
    private List<Map<String, String>> mappings = new ArrayList<>();

    @Override
    public void process(HttpRequest request, HttpResponse response, Socket clientSocket) {
        request.absPath = getAbsPath(request);
        System.out.println("absPath: " + request.absPath);
    }
    
    String getAbsPath(HttpRequest request) {
        String basePath = System.getProperty("user.dir"); 
        String relativePath = stripPatternFromPath(request.path, getUrlPattern(request.path));
        Path path = Paths.get(basePath, relativePath).toAbsolutePath();
        return path.toString();
    }

    // URL 패턴 부분을 제거하여 남은 경로만 반환
    private String stripPatternFromPath(String urlPath, String urlPattern) {
        // URL 패턴 부분을 제거하여 남은 경로만 반환
        if (urlPattern.endsWith("/*")) {
            String basePattern = urlPattern.replace("/*", "");
            if (urlPath.startsWith(basePattern)) {
                return urlPath.substring(basePattern.length());
            }
        }
        return urlPath;  // 패턴이 없는 경우 원본 URL 경로 반환
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

}
