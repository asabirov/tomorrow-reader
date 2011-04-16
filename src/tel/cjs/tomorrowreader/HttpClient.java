/**
 * HTTP клиент для связи с сервером
 */
package tel.cjs.tomorrowreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import android.util.Log;
import java.util.List;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
/**
 *  1) Get the value for a key: jsonObjRecv.get("key");
 *  2) Get a nested JSONObject: jsonObjRecv.getJSONObject("key")
 *  3) Get a nested JSONArray: jsonObjRecv.getJSONArray("key") 
 **/
public class HttpClient {
    private static final String TAG = "zvtra";
        
    public static String POST = "POST";
    
    public static String GET = "GET";
    
    public static String DELETE = "DELETE";
    
    public static JSONObject SendHttpRequest(String method, String URL, List params) {
        try {
           DefaultHttpClient httpclient = new DefaultHttpClient();
           long t = System.currentTimeMillis();
           HttpResponse response;
           
           Log.d(TAG, "Send request " + URL + " params: " + params.toString());
           
           if (method == POST){
                HttpPost request = new HttpPost(URL);
                request.setEntity(new UrlEncodedFormEntity(params));   
                request.setHeader("Accept", "application/json");
                request.setHeader("Accept-Encoding", "gzip"); 
                response = (HttpResponse) httpclient.execute(request);
           }  else if (method == DELETE) {
               String paramString = URLEncodedUtils.format(params, "utf-8");
               if(!URL.endsWith("?")) {
                   URL += "?";
               }
               HttpDelete request = new HttpDelete(URL + paramString);
               request.setHeader("Accept", "application/json");
               request.setHeader("Accept-Encoding", "gzip"); 
               response = (HttpResponse) httpclient.execute(request);
           } else  { //GET
               String paramString = URLEncodedUtils.format(params, "utf-8");
               if(!URL.endsWith("?")) {
                   URL += "?";
               }
              
               HttpGet request = new HttpGet(URL + paramString);
               request.setHeader("Accept", "application/json");
               request.setHeader("Accept-Encoding", "gzip"); 
               response = (HttpResponse) httpclient.execute(request);               
           }
           
           Log.i(TAG, "HTTPResponse received in [" + (System.currentTimeMillis()-t) + "ms]");

           HttpEntity entity = response.getEntity();

           if (entity != null) {
                InputStream instream = entity.getContent();
                Header contentEncoding = response.getFirstHeader("Content-Encoding");
                if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                    instream = new GZIPInputStream(instream);
                }

                String resultString= convertStreamToString(instream);
                instream.close();
                JSONObject jsonObjRecv = new JSONObject(resultString);
                Log.i(TAG, jsonObjRecv.toString());
                return jsonObjRecv;
           } 
        } catch (Exception e) {
           e.printStackTrace();
        }
        return null;
    }
    
    /**   
     * (c) public domain: http://senior.ceng.metu.edu.tr/2009/praeda/2009/01/11/a-simple-restful-client-at-android/
     **/  
    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();        
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}