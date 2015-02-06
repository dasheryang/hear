package hear.app.engine;

import hear.app.util.LogUtil;

import java.util.Map;

import android.os.AsyncTask;
import android.util.Pair;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Created by power on 14-3-10.
 */
abstract public class BaseHttpAsyncTask extends AsyncTask<String,Void,JsonRespWrapper> {

    protected String _resp=null;


    public String method= HttpRequest.METHOD_GET;

    public String _url;

    public BaseHttpAsyncTask(String url){
        _url=url;
    }

    /*
    private boolean needJson=false;

    public BaseHttpAsyncTask needJson(boolean b){
        needJson=b;
        return this;
    }
    */

    public Map<?,?> httpParams=null;

    public BaseHttpAsyncTask post(Map params){
        this.method=HttpRequest.METHOD_POST;
        this.httpParams=params;
        return this;
    }

    public BaseHttpAsyncTask get(Map params){
        this.httpParams=params;
        return this;
    }

    public BaseHttpAsyncTask post(){
        return post(null);
    }

    public void run(){
        this.execute();
    }

    public static Pair<Integer,String> getLocalError(){
        return new Pair<Integer, String>(RespCode.COMM_ERROR,RespCode.getCommErrorMsg());
    }

    protected TimeDelta timeDelta=new TimeDelta();

    protected HttpEngine httpEngine=new HttpEngine();


    public Class<JsonRespWrapper> getRespClass(){
        return JsonRespWrapper.class;
    }

    @Override
    protected JsonRespWrapper doInBackground(String... params) {
        try {

            if(this.method==HttpRequest.METHOD_GET){
                _resp= httpEngine.get(_url,httpParams);
            }
            else{
                _resp= httpEngine.post(_url, httpParams);
            }
            LogUtil.d(String.format("get %s use %s", _url, timeDelta.getDelta()));
            JsonRespWrapper wrapper=GSON.fromJson(_resp,getRespClass());
            LogUtil.d(String.format("after gson parse to wrapper %s",timeDelta.getDelta()));
            return wrapper;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JsonRespWrapper(JsonRespWrapper.LOCAL_ERROR,JsonRespWrapper.LOCAL_ERROR_MSG);
    }

    public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
}
