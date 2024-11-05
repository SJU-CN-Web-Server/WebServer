package webserver.data;

import java.util.Optional;

public class HttpRequest {
    public String host;
    public String method;
    public String path;
    public String version;
    public String userAgent;
    public String accept; 
    public String query;
    public String body;
    public Integer port;
    public Optional<String> connection = Optional.empty();
    public String keepAlive;
    public String rawData; 
    public String acceptLanguage; 
    public String acceptEncoding; 
    public String acceptCharset; 

    public boolean isDirectory;

    public String ifModifiedSince;
    public String if_none_match;
    public String absPath;
}
