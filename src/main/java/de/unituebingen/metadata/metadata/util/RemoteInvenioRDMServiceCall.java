package de.unituebingen.metadata.metadata.util;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;


public class RemoteInvenioRDMServiceCall {


    private static CloseableHttpClient getHTTPClient() {

        @SuppressWarnings("deprecation")
        CloseableHttpClient client;
        try {
            client = HttpClients.custom()
                    .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
                    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();

            return client;
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            System.out.println(e);
            return null;
        }
    }

    // #################################################################################################

    public static String postDataciteRecordToRemoteInvenioService(String datacitejsonString, String apiKey) {

        try {

            System.out.println("Trying to start Request");

            System.out.println("apiKey");
            System.out.println(apiKey);

            System.out.println("datacitejsonString");
            System.out.println(datacitejsonString);

            CloseableHttpClient client = getHTTPClient();

            HttpPost httpPost = new HttpPost("https://inveniordm.web.cern.ch/api/records");

            httpPost.setEntity(new StringEntity(datacitejsonString, ContentType.APPLICATION_JSON));

            httpPost.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            httpPost.addHeader(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml,application/json;");
            httpPost.setHeader(HttpHeaders.AUTHORIZATION,
                    "Bearer " + apiKey);

            CloseableHttpResponse response = client.execute(httpPost);

            // HttpGet httpGet = new HttpGet("https://inveniordm.web.cern.ch/api/records/72k1x-nyb37/draft");

            // httpGet.addHeader(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml,application/json;");
            // httpGet.setHeader(HttpHeaders.AUTHORIZATION,
            //         "Bearer " + apiKey);

            // CloseableHttpResponse response = client.execute(httpGet);

            // System.out.println("response");
            // System.out.println(response);

            InputStream responseInvenio = response.getEntity().getContent(); // wrappedEntity as application/json // contains PID

            System.out.println("responseInvenio");
            System.out.println(responseInvenio);

            JSONObject jsonResponseInvenio = new JSONObject(IOUtils.toString(responseInvenio, "UTF-8"));

            System.out.println("jsonResponseInvenio");
            System.out.println(jsonResponseInvenio);

            // #####################################################################

            String pid = jsonResponseInvenio.get("id").toString();

            JSONObject linkList = (JSONObject) jsonResponseInvenio.get("links");

            String reviewLink = linkList.get("review").toString();

            String registerDoiLink = linkList.get("reserve_doi").toString();

            String draftFiles = linkList.get("files").toString();

            // #####################################################################

            // Request a DOI to be registered for the resource
            registerDOI(registerDoiLink, apiKey);

            // Assign resource to a community and receive a Link (id) to submit resource for review
            String submitReviewLink = assignCommunity(reviewLink, apiKey);

            System.out.println("submitReviewLink");
            System.out.println(submitReviewLink);

            // Request a review from a community / send record to a community
            // requestCommunityReview(submitReviewLink, apiKey);

            client.close();

            return pid;

        } catch (Exception ex) {
            ex.getLocalizedMessage();
            System.out.println(ex);
            return "";
        }
    }

        // #################################################################################################

    private static void draftDataFileUpload(File dataFile, String pid, String token, String draftFiles) {

        try {

            JSONArray fileArray = new JSONArray();
            JSONObject fileObject = new JSONObject();
            fileObject.put("key", dataFile.getName());

            fileArray.put(fileObject);

            CloseableHttpClient client = getHTTPClient();

            HttpPost httpPost = new HttpPost(draftFiles);

            httpPost.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

            httpPost.setEntity(new StringEntity(fileArray.toString(), ContentType.APPLICATION_JSON));

            CloseableHttpResponse response = client.execute(httpPost);

            client.close();

        } catch (Exception ex) {
            ex.getLocalizedMessage();
        }
    }

    // #################################################################################################

    private static void uploadDataFile(File dataFile, String pid, String token, String draftFiles) {

        try {

            FileEntity requestEntity = new FileEntity(dataFile);

            CloseableHttpClient client = getHTTPClient();

            HttpPut httpPut = new HttpPut(draftFiles + "/" + dataFile.getName() + "/content");

            httpPut.addHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
            //httpPut.addHeader(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml,application/json;");
            httpPut.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

            httpPut.setEntity(requestEntity);

            CloseableHttpResponse response = client.execute(httpPut);

            client.close();

        } catch (Exception ex) {
            ex.getLocalizedMessage();
        }
    }

    // #################################################################################################

    private static void commitDataFileUpload(File dataFile, String pid, String apiKey, String draftFiles) {

        try {

            CloseableHttpClient client = getHTTPClient();

            HttpPost httpPost = new HttpPost(draftFiles + "/" + dataFile.getName() + "/commit");

            httpPost.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);

            CloseableHttpResponse response = client.execute(httpPost);

            client.close();

        } catch (Exception ex) {
            ex.getLocalizedMessage();
        }
    }

        // #################################################################################################

        private static String assignCommunity(String reviewLink, String apiKey) {

            try {
 
                JSONObject review = new JSONObject();
                review.put("type", "community-submission");
            
                JSONObject receiver = new JSONObject();
                receiver.put("community", "2cde3517-1c1b-42f3-a352-188f11a2d0c7");

                review.put("receiver", receiver);

            // ######################################################
    
                CloseableHttpClient client = getHTTPClient();

                System.out.println("reviewLink");
                System.out.println(reviewLink);
    
                HttpPut httpPut = new HttpPut(reviewLink);
    
                httpPut.setEntity(new StringEntity(review.toString(), ContentType.APPLICATION_JSON));
                httpPut.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                httpPut.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
    
                CloseableHttpResponse response = client.execute(httpPut);
                InputStream responseInvenio = response.getEntity().getContent(); // wrappedEntity as application/json // contains ID

                System.out.println("responseInvenioCommunity");
                System.out.println(responseInvenio);

                JSONObject jsonResponseInvenio = new JSONObject(IOUtils.toString(responseInvenio, "UTF-8"));

                System.out.println("jsonResponseInvenioCommunity");
                System.out.println(jsonResponseInvenio);

                JSONObject linkList = (JSONObject) jsonResponseInvenio.get("links");
                JSONObject submit = (JSONObject) linkList.get("actions");
                String submitLink = submit.get("submit").toString();
                
                client.close();
                
                return submitLink;

            } catch (Exception ex) {
                ex.getLocalizedMessage();
                return new String();
            }
        }

    // #################################################################################################

    private static void registerDOI(String registerDoiLink, String apiKey) {

        try {

            CloseableHttpClient client = getHTTPClient();

            HttpPost httpPost = new HttpPost(registerDoiLink);

            httpPost.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);

            CloseableHttpResponse response = client.execute(httpPost);

            client.close();

        } catch (Exception ex) {
            ex.getLocalizedMessage();
        }
    }  
    
    // #################################################################################################

    private static void requestCommunityReview(String submitReviewLink, String apiKey) {

        try {

            CloseableHttpClient client = getHTTPClient();

            HttpPost httpPost = new HttpPost(submitReviewLink);

            httpPost.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);

            CloseableHttpResponse response = client.execute(httpPost);

            client.close();

        } catch (Exception ex) {
            ex.getLocalizedMessage();
        }
    }            
}