package API;


import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Connection file
 * Handles communication with websites
 * Does the work of actually making requests to servers
 * and ensuring the responses are valid
 */
public class Connection{

    public static final int OK_STATUS=200;
    public static final boolean PRINT_CONNECTIONS=true;
    private final HttpClient client;
    private boolean ignoreStatus=false;

    public Connection() {
        client = Cert.getClientWithCert();
    }

    public void setIgnoreStatus(boolean ignoreStatus){
        this.ignoreStatus=ignoreStatus;
    }

    public HttpResponse<String> get(URI uri, String[] headers) {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .GET();
        if (headers.length != 0) {
            request.headers(headers);
        }
        HttpResponse<String> response = doRequest(uri, request);
        return response;
    }

    public HttpResponse<String> post(URI uri, String[] headers, HttpRequest.BodyPublisher body) {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .POST(body);
        if (headers.length != 0) {
            request.headers(headers);
        }
        HttpResponse<String> response = doRequest(uri, request);
        return response;
    }

    public HttpResponse<String> put(URI uri, String[] headers) {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.noBody());
        if (headers.length != 0) {
            request.headers(headers);
        }
        HttpResponse<String> response = doRequest(uri, request);
        return response;
    }

    private HttpResponse<String> doRequest(URI uri, HttpRequest.Builder builder) {
        if(PRINT_CONNECTIONS) {
            System.out.println("Request to " + uri.toString());
        }
        HttpResponse<String> response = null;
        try {
            HttpRequest request = builder.uri(uri).build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode()!=OK_STATUS && !ignoreStatus){
                System.out.println("Status code "+response.statusCode());
                System.out.println(response.body());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }


}