package webserver;

import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import webserver.data.HttpRequest;
import webserver.data.HttpResponse;

public class HttpCachingHandler extends HttpHandler{
    private Instant lastModified;
    private Instant ifModifiedSince;
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;
    DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
            .withZone(ZoneOffset.UTC);

    DateTimeFormatter formatter = new DateTimeFormatterBuilder()
        .appendPattern("EEE, dd MMM yyyy HH:mm:ss")
        .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
        .appendLiteral(" GMT")
        .toFormatter();

    @Override
    public void process(HttpRequest request, HttpResponse response, Socket clientSocket) {
        this.httpRequest = request;
        this.httpResponse = response;
        if(request.ifModifiedSince != null
        || request.if_none_match != null) {
            handleCacheRequest();
        } else {
            handleNonCacheRequest();
        }
    }

    private void handleCacheRequest(){
        setLastModifiedTime();
        setIfmodifiedSinceTime();
        //캐시 로직 처리
        try{
            if(lastModified.isAfter(ifModifiedSince)){
                httpResponse.status = 200;
                httpResponse.cache_control = "public, " +getCacheControlMaxAge(); 

                // 포맷터 설정

                // Instant를 ZonedDateTime으로 변환 후 포맷
                String formattedDate = formatter1.format(lastModified);

                httpResponse.last_modified = formattedDate;
            }
            else{
                // 요청된 캐시가 유효함. 캐시 데이터 반환을 위해 304 상태 코드 반환
                httpResponse.status = 304;
                httpResponse.body = "\r\n";
                httpResponse.contentLength = 0;
                setGoToResponse(true);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private String getCacheControlMaxAge() {
        // Default TTL value
        int defaultTTL = 3600;
        int ttlValue = defaultTTL;
        
        // Define the path to the settings.json file in the user's directory
        String userDir = System.getProperty("user.dir");
        Path settingsPath = Paths.get(userDir, "settings.json");

        if(!Files.exists(settingsPath)) {
            System.out.println("settings.json not found. Using default value of 3600");
            return "max-age=" + ttlValue;
        }

        try (InputStream input = Files.newInputStream(settingsPath)) {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonMap = objectMapper.readValue(input, Map.class);
            
            // Retrieve the TTL value from the JSON if present, else use the default
            if (jsonMap.containsKey("ttl")) {
                ttlValue = (int) jsonMap.get("ttl");
            } else {
                System.out.println("\"ttl\" not found in settings.json, using default value of 3600");
            }
        } catch (Exception e) {
            System.out.println("Failed to load TTL from settings.json, using default value of 3600");
        }

        return "max-age=" + ttlValue;
    }

    private void handleNonCacheRequest(){
        if(setLastModifiedTime()){
            String formattedDate = formatter1.format(lastModified);
            httpResponse.status = 200;
            httpResponse.cache_control = "public, " +getCacheControlMaxAge(); //temporary, 캐시 정책 설정필요.
            httpResponse.last_modified = formattedDate; // temporary, 캐시 정책 설정필요.
            // response.cache_expires = "Wed, 21 Oct 2030 07:28:00 GMT"; // temporary, 캐시 정책 설정필요.
        }
    }

    //utils
    private boolean setLastModifiedTime() {
        Path path = Path.of(httpRequest.absPath);
        if(!Files.exists(path)) {
            return false;
        }
        try {
            this.lastModified = Files.getLastModifiedTime(path).toInstant().truncatedTo(ChronoUnit.SECONDS);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    private void setIfmodifiedSinceTime(){
        ZonedDateTime ifModifiedSinceZdt = ZonedDateTime.parse(this.httpRequest.ifModifiedSince, formatter);
        ifModifiedSince = ifModifiedSinceZdt.toInstant();
    }
}