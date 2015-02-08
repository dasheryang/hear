package hear.app.engine;

import hear.app.helper.LogUtil;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.params.HttpParams;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;



/**
 * Created by power on 14-6-2.
 */
public class HttpEngine {

    public static final String VERSION_CONFIG_URL ="http://static.dbmeizi.com/dbmeizi/version_config.js";

    public static final String APP_CONFIG_URL =HttpEngine.getURL("/m/config") ;

    public static final String APP_LOGIN_URL =HttpEngine.getURL("/m/login") ;

    public static Map<String,String> hostMap=new HashMap<String, String>();


    /**
     * GSON instance to use for all request  with date format set up for proper parsing.
     * <p/>
     * You can also configure GSON with different naming policies for your API.
     * Maybe your API is Rails API and all json values are lower case with an underscore,
     * like this "first_name" instead of "firstName".
     * You can configure GSON as such below.
     * <p/>
     *
     * public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd")
     *         .setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES).create();
     */
    public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    /**
     * Read and connect timeout in milliseconds
     */
    private static final int TIMEOUT = 30 * 1000;



    private static class JsonException extends IOException {

        private static final long serialVersionUID = 3774706606129390273L;

        /**
         * Create exception from {@link com.google.gson.JsonParseException}
         *
         * @param cause
         */
        public JsonException(final JsonParseException cause) {
            super(cause.getMessage());
            initCause(cause);
        }
    }


    public HttpEngine() {

    }

    /**
     * Execute request
     *
     * @param request
     * @return request
     * @throws java.io.IOException
     */
    protected HttpRequest execute(final HttpRequest request) throws IOException {
        if (!configure(request).ok())
            throw new IOException("Unexpected response code: " + request.code());
        return request;
    }

    public String getUserAgent(){
        return "app=com.hear";
    }

    public HttpRequest addCredentialsTo(HttpRequest request){
        //TODO add credentials
        return request;
    }

    private HttpRequest configure(final HttpRequest request) {
        request.connectTimeout(TIMEOUT).readTimeout(TIMEOUT);

        request.userAgent(getUserAgent());

        if (isPostOrPut(request)) {
            // All PUT & POST requests to Parse.com api must be in JSON
            // https://www.parse.com/docs/rest#general-requests
            request.contentType("application/json");
        }

        return addCredentialsTo(request);
    }

    private boolean isPostOrPut(final HttpRequest request) {
        return request.getConnection().getRequestMethod().equals(HttpRequest.METHOD_POST)
                || request.getConnection().getRequestMethod().equals(HttpRequest.METHOD_PUT);

    }





    public static String getURL(String path,HttpParams urlParams){

        //FIXME
        String url="http://dev.dbmeizi.com";

        url = url+path;

        if(urlParams!=null && !TextUtils.isEmpty(urlParams.toString())){
            url = url + (url.indexOf("?")>=0?"&":"?")+urlParams.toString();
        }
        return url;
    }

    public static String getURL(String path){
        return getURL(path, null);
    }


    public String get(String url,Map<?,?> params) throws IOException{

        URL urlObj = new URL(url);

        String oldHost = urlObj.getHost();
        String fixedIp = hostMap.get(oldHost);
        boolean needSetHostHeader=false;
        if(!TextUtils.isEmpty(fixedIp)){
            url = url.replace(oldHost,fixedIp);
            needSetHostHeader=true;
        }
        HttpRequest request=HttpRequest.get(url,params,false);

        if(needSetHostHeader){
            LogUtil.d("replace host:"+oldHost);
            request.header("Host",oldHost);
        }

        execute(request);
        return request.body();
    }

    public String post(String url,Map<?,?> params) throws IOException{
        HttpRequest request=execute(HttpRequest.post(url,params,false));
        return request.body();
    }
 
}
