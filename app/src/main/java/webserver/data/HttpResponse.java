package webserver.data;

import java.util.Date;

public class HttpResponse {
    public String version;
    public Integer status;
    public String body;
    public String contentType;
    public Integer contentLength;
    public String keepAlive;
    public String connection;
    public String rawData; //추가
    public String cache_control;
    public String cache_expires;
    public String last_modified;
    public String date;
    public Date _date;
}
