package hear.app.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPreferencesCache {
    private Context context;

    private String name;

    public SharedPreferencesCache(Context context, String name) {
        this.context = context;
        this.name = name;
    }

    public void remove(String key) {
        Editor sharedata = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        sharedata.remove(key);
        sharedata.commit();
    }

    /**
     * 保存数据
     *
     * @param key
     * @param value
     */
    public void put(String key, String value) {
        Editor shareData = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        shareData.putString(key, value);
        shareData.apply();
    }

    /**
     * 保存数据
     *
     * @param key
     * @param value
     */
    public void put(String key, int value) {
        Editor shareData = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        shareData.putInt(key, value);
        shareData.apply();
    }

    /**
     * 保存数据
     *
     * @param key
     * @param value
     */
    public void put(String key, boolean value) {
        Editor shareData = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        shareData.putBoolean(key, value);
        shareData.apply();
    }

    /**
     * 获取数据
     *
     * @param key
     */
    public String get(String key) {
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);

        return sp.getString(key, null);
    }

    public String get(String key, String defVal) {
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return sp.getString(key, defVal);
    }

    public boolean get(String key, boolean defValue) {
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return sp.getBoolean(key, defValue);
    }

    public int get(String key, int defValue) {
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);

        return sp.getInt(key, defValue);
    }
}
