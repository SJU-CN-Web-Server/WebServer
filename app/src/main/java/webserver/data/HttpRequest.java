package webserver.data;

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
    public String connection;
    public String keepAlive;
    public String rawData; //추가
    public String acceptLanguage; //추가
    public String acceptEncoding; //추가
    public String acceptCharset; //추가
    // public String keepAlive; //추가
    // public String connection; //추가

    public boolean isDirectory;

    public String if_modified_since;
    public String if_none_match;
    public String absPath;
}