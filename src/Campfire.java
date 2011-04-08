/**
 * Created by IntelliJ IDEA.
 * User: apearson
 * Date: Sep 8, 2010
 * Time: 3:01:45 PM
 * To change this template use File | Settings | File Templates.
 */

import jetbrains.buildServer.log.Loggers;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;
import java.net.URLEncoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Campfire {
    private String myToken;
    private String myUrl;
    private String myProxyHost;
    private int myProxyPort;
    private AuthScope myScope;
    private AuthScope myProxyScope;
    private UsernamePasswordCredentials myCredentials;
    private UsernamePasswordCredentials myProxyCredentials;

    public Campfire(String token, String url, Boolean useSSL, String proxyHost, int proxyPort, String proxyUser, String proxyPass) {
        myToken = token;
        myUrl = url;
        myScope = new AuthScope(
                myUrl.replaceAll("http(s)?://", ""),
                useSSL ? 443 : 80
        );

        myCredentials = new UsernamePasswordCredentials(myToken, "X");

        if(proxyHost != null && proxyHost.length() > 0)
        {
            myProxyHost = proxyHost;
            myProxyPort = proxyPort;

            if(proxyUser != null && proxyUser.length() > 0)
            {
                myProxyCredentials = new UsernamePasswordCredentials(proxyUser, proxyPass);
                myProxyScope = new AuthScope(proxyHost, proxyPort);
            }

        }
    }

    public Boolean postMessage(String roomNumber, String message) {
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            
            if(myProxyHost != null && myProxyHost.length() > 0)
            {
                HttpHost proxy = new HttpHost(myProxyHost, myProxyPort);
                client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

                if(myProxyScope != null && myProxyCredentials != null)
                {
                    client.getCredentialsProvider().setCredentials(myProxyScope, myProxyCredentials);
                }
            }
            
            client.getCredentialsProvider().setCredentials(myScope, myCredentials);
            
            String postUrl = myUrl + "/room/" + roomNumber + "/speak.xml";
            HttpPost post = new HttpPost(postUrl);

            String bodyText = "<message><body>" + message + "</body></message>";
            StringEntity postBody = new StringEntity(bodyText);
            post.setEntity(postBody);
            post.setHeader("Content-Type", "application/xml");
            
            Loggers.SERVER.info("Executing post: " + postUrl);
            Loggers.SERVER.info("With this body: " + bodyText);
            HttpResponse response = client.execute(post);
            Loggers.SERVER.info("Response result: " + response.getStatusLine());

            HttpEntity entity = response.getEntity();
            
            if (entity != null) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(entity.getContent()));

                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    Loggers.SERVER.info(inputLine);
            }
            return true;
        }
        catch (IOException e) {
            Loggers.SERVER.error("Something failed... ", e);
            return false;
        }
    }
}
