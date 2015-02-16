package hear.app.helper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ZhengYi on 15/2/15.
 */
public class FileUtils {
    private FileUtils() {
    }

    @Nullable
    public static String readStringFromFile(@NonNull File file) {

        try {
            FileInputStream input = new FileInputStream(file);
            byte[] buffer = new byte[input.available()];
            input.read(buffer, 0, input.available());
            return new String(buffer);
        } catch (IOException ignored) {
        }

        return null;
    }

    public static boolean writeStringToFile(@NonNull File file, @NonNull String text, boolean deleteIfExisted) {

        if (file.exists()) {
            if (!deleteIfExisted)
                return true;
            else
                file.delete();
        }
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
            FileOutputStream output = new FileOutputStream(file);
            byte[] buffer = text.getBytes();
            output.write(buffer, 0, buffer.length);
            output.flush();
            output.close();
            return true;
        } catch (IOException ignored) {
        }


        return false;
    }
}
