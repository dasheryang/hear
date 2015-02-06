package hear.app.engine;

/**
 * Created by power on 14-6-2.
 */
public  class JsonRespWrapper {
    public static final int LOCAL_ERROR=-1;
    public static final String LOCAL_ERROR_MSG="请求服务器失败，请重新再试";
    public int ret;
    public String reason;

    public JsonRespWrapper(int c, String s){
        this.ret =c;
        this.reason =s;
    }
    public JsonRespWrapper(){
    }
}