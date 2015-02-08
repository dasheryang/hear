package hear.app.helper;

import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Created by power on 14-3-22.
 */
public class AppContext {
    public static Application getContext(){
        return _mContext;
    }

    private static Application _mContext;

    public static void setAppContext(Application context){
        _mContext=context;
    }

    private static SharedPreferencesCache preferencesCache;

    public static SharedPreferencesCache getSharedPrefernce(){
        if(preferencesCache==null){
            preferencesCache=new SharedPreferencesCache(_mContext,_mContext.getPackageName());
        }
        return preferencesCache;
    }

    static Gson gson=null;

    public static Gson getGSON(){
        if(gson==null){
            gson= new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        }
        return gson;
    }

    public static LayoutInflater getLayoutInflater(){
        return (LayoutInflater) AppContext.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public static String getPackageName(){
        return getContext().getPackageName();
    }
}

