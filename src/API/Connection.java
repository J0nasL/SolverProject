package API;


import java.io.*;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Connection file
 * Handles communication with websites
 * Does the work of actually making requests to servers
 * and ensuring the responses are valid
 */
public class Connection{

    public static final int OK_STATUS=200;
    //public static final int RETRY_CODE=503;
    public static final int RETRY_CODE=503;
    public static final int OLD_TOKEN_CODE=401;
    public static final int MAX_RETRIES=3;
    public static final boolean PRINT_CONNECTIONS=true;
    private final HttpClient client;
    private boolean ignoreStatus=false;

    public Connection(){
        client=Cert.getClientWithCert();
    }

    public void setIgnoreStatus(boolean ignoreStatus){
        this.ignoreStatus=ignoreStatus;
    }

    public HttpResponse<String> get(URI uri, String[] headers){
        HttpRequest.Builder request=HttpRequest.newBuilder()
                .GET();
        if (headers.length!=0){
            request.headers(headers);
        }
        HttpResponse<String> response=doRequest(uri, request);
        return response;
    }

    public HttpResponse<String> post(URI uri, String[] headers, HttpRequest.BodyPublisher body){
        HttpRequest.Builder request=HttpRequest.newBuilder()
                .POST(body);
        if (headers.length!=0){
            request.headers(headers);
        }
        HttpResponse<String> response=doRequest(uri, request);
        return response;
    }

    public HttpResponse<String> put(URI uri, String[] headers){
        HttpRequest.Builder request=HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.noBody());
        if (headers.length!=0){
            request.headers(headers);
        }
        HttpResponse<String> response=doRequest(uri, request);
        return response;
    }

    private HttpResponse<String> doRequest(URI uri, HttpRequest.Builder builder){
        if (PRINT_CONNECTIONS){
            System.out.println("Request to " + uri.toString());
        }
        for (int i=0; i <= MAX_RETRIES; i++){

            HttpRequest request=builder.uri(uri).build();
            HttpResponse<String> response;

            try{
                response=client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (ConnectException e){
                System.out.println("ConnectionException:\nCannot connect to " + ConnectionURI.ON_DEMAND);
                return null;
            } catch (IOException | InterruptedException e){
                e.printStackTrace();
                return null;
            }

            int status=response.statusCode();

            if (status!=OK_STATUS && !ignoreStatus){
                if (status==RETRY_CODE){
                    System.out.println("Connection lost.\nRetrying...");

                } else if (status==OLD_TOKEN_CODE){
                    //TODO update the token
                    System.out.println("Your login token is out of date");
                    System.out.println("Restart the application to get a new token");
                } else{
                    System.out.println("Status code " + status);
                    System.out.println(response.body());
                    return null;
                }
            } else{
                return response;
            }

        }
        return null;
    }
}