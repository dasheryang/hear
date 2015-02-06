package hear.app.engine;

/**
 * Created by power on 14-6-2.
 */
public class RespCode {
    public static final int COMM_ERROR =-1;

    public static String getCommErrorMsg() {
        return "请求服务器失败，请重试!";
    }
}

