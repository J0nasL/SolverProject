package API;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public class RunnableConnection implements Runnable{
    public static enum method{
        GET, POST, PUT
    }

    private method curMethod;
    private URI curURI;
    private String[] curHeaders;
    private HttpRequest.BodyPublisher curBody;
    public HttpResponse<String> response;

    RunnableConnection(method m, URI uri, String[] headers, HttpRequest.BodyPublisher body){
        curMethod=m;
        curURI=uri;
        curHeaders=headers;

        curBody=Objects.requireNonNullElseGet(body, HttpRequest.BodyPublishers::noBody);
    }

    public void run(){
        Connection c=new Connection(); //make this async bc might be a little slow
        switch (curMethod){
            case GET -> response=c.get(curURI, curHeaders);
            case PUT -> response=c.put(curURI, curHeaders);
            case POST -> response=c.post(curURI, curHeaders, curBody);
        }

    }
}
