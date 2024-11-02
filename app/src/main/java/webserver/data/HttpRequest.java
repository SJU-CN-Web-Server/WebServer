package webserver.data;

import java.util.Optional;

public class HttpRequest {
    public String host;
    public String method;
    public String path;
    public String version;
    public String userAgent;
    public String acceptLanguage;
    public String query;
    public String body;
    public Integer port;

    public Optional<String> if_modified_since;
    public Optional<String> if_none_match;
    public String absPath;
}
