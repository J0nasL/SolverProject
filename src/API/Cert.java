package API;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Gives an HTTPClient with certificate properties set to accept certificates from cert.pem
 */
public class Cert{
    private static SSLContext getCert(){
        try{
            InputStream is=new FileInputStream("cert.pem");
            // You could get a resource as a stream instead.

            CertificateFactory cf=CertificateFactory.getInstance("X.509");
            X509Certificate caCert=(X509Certificate) cf.generateCertificate(is);

            TrustManagerFactory tmf=TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore ks=KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null); // You don't need the KeyStore instance to come from a file.
            ks.setCertificateEntry("caCert", caCert);

            tmf.init(ks);

            SSLContext sslContext=SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            return sslContext;


        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * IT DOES THE THING!!!!
     * Returns an instance of HttpClient that accepts certificates stored in cert.pem
     */
    public static HttpClient getClientWithCert(){
        HttpClient client=HttpClient.newBuilder()
                // Other initialization
                .sslContext(getCert()).build();
        return client;
    }
}
