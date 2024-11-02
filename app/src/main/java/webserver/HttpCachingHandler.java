package webserver;

import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import webserver.data.HttpRequest;
import webserver.data.HttpResponse;

public class HttpCachingHandler extends HttpHandler{
    @Override
    public void process(HttpRequest request, HttpResponse response, Socket clientSocket) {
        if(request.if_modified_since.isPresent() 
        || request.if_none_match.isPresent()) {
            handleCacheRequest(request, response);
        } else {
            handleNonCacheRequest(response);
        }
    }

    // private boolean isCacheable(HttpRequest request){
    //     return true;
    // }

    
    private void handleCacheRequest(HttpRequest request, HttpResponse response){
        //캐시 로직 처리
        String filePath = request.absPath;
        Path path = Path.of(filePath);
        try{
            Instant lastModified = Files.getLastModifiedTime(path).toInstant();
            Instant ifModifiedSince = Instant.parse(request.if_modified_since.get());

            if(lastModified.isAfter(ifModifiedSince)){
                response.status = 200;
                // response.body = "Hello World";
                // response.contentLength = 11;
                // response.contentType = "text/plain";
                response.cache_control = "public, max-age=60"; //temporary
                response.cache_expires = "Wed, 21 Oct 2015 07:28:00 GMT"; // temporary
            }
            else{
                // 요청된 캐시가 유효함. 캐시 데이터 반환을 위해 304 상태 코드 반환
                response.status = 304;
                response.body = "";
                response.contentLength = 0;
                setGoToResponse(true);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleNonCacheRequest(HttpResponse response){
        //캐시 로직 처리
        response.status = 200;
        // response.body = "Hello World";
        // response.contentLength = 11;
        // response.contentType = "text/plain";
        response.cache_control = "public, max-age=60"; //temporary, 캐시 정책 설정필요.
        response.cache_expires = "Wed, 21 Oct 2015 07:28:00 GMT"; // temporary, 캐시 정책 설정필요.
    }
}
