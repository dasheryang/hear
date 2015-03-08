package hear.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import hear.app.helper.HttpActionUtil;
import hear.app.helper.NetUtil;
import hear.app.helper.SDCardUtils;
import hear.app.models.Article;
import hear.app.store.ArticleStore;

/**
 * @author will.yao 缓存音频文件
 */
public class CacheMediaService extends Service {

    private static final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
    public static final ThreadPoolExecutor ES = new ThreadPoolExecutor(3, 3,
            60 * 60, TimeUnit.SECONDS, queue);

    private static final String TAG = "CacheMediaService";

    private static final String NOT_FINISH_FILE_SUFFIX = ".hh";

    private String lastArticleJson = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String json = null;
        try {
            json = intent.getStringExtra(Article.KEY_ALL_ARTICLE);
        } catch (Exception e) {
        }

        if (json == null || lastArticleJson == null
                || !lastArticleJson.equals(json)) {
            if (!TextUtils.isEmpty(json)) {
                lastArticleJson = json;

            }
            cacheMediaAction(ArticleStore.getInstance().getArticleSet());
        }
        ES.submit(new Runnable() {

            @Override
            public void run() {
                deleteOutOfDataMediaFile();
            }
        });

        return START_STICKY;
    }

    /**
     * 缓存最新的文章音频文件
     */
    public void cacheMediaAction(List<Article> lastArticles) {

        if (!NetUtil.isWiFiActive()) {
            return;
        }

        // 循环编历所有最新的文章音频，自动断点续传下载MP3文件。比较MD5值，如果一样，则说明文件已成功下载完成。未下载成功的文件名后缀为.hh，下载成功以后为.mp3

        String soundUrl = null;
        String mediaName = null;
        String md5 = null;
        File mediaFile = null;

        for (Article article : lastArticles) {
            // http://www.hearheart.com/sounds/2014-10-29695cf8f0a73cfde1c394711a48cf2c9d.mp3
            soundUrl = article.soundurl;
            if (!TextUtils.isEmpty(soundUrl)) {
                mediaName = soundUrl.substring(soundUrl.lastIndexOf("/") + 1);
                md5 = mediaName.substring(mediaName.lastIndexOf("-") + 3,
                        mediaName.lastIndexOf(".mp3"));
                Log.e(TAG, "mediaName " + mediaName + " md5 " + md5);

                mediaFile = getCacheMediaFile(mediaName);
                if (mediaFile.exists()
                        && md5.toLowerCase().equals(
                        getFileMd5(mediaFile.getPath()).toLowerCase())) {
                    Log.d(TAG, "文章音频 " + mediaName + " 已存在，无需下载");
                } else {
                    boolean continueDown = false;
                    if (!mediaFile.exists()) {
                        // 如果不存在，看临时文件在不在
                        mediaFile = getCacheMediaFile(mediaName
                                + NOT_FINISH_FILE_SUFFIX);
                        if (mediaFile.exists()) {
                            // 存在则继续下载
                            continueDown = true;
                        } else {
                            // 不存在则创建文件
                            try {
                                mediaFile.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (mediaFile.exists()) {
                                continueDown = true;
                            } else {
                                Log.e(TAG, "创建文件" + mediaName + "失败");
                            }
                        }
                        if (continueDown) {
                            final File finalMediaFile = mediaFile;
                            final String finalSoundUrl = soundUrl;
                            final String finalMediaName = mediaName;
                            ES.submit(new Runnable() {

                                @Override
                                public void run() {
                                    boolean result = HttpActionUtil.download(
                                            finalSoundUrl, finalMediaFile);
                                    if (result) {
                                        Log.d(TAG,
                                                "文章音频 "
                                                        + finalMediaFile
                                                        .getName()
                                                        + " 下载成功");
                                        boolean renameResult = finalMediaFile
                                                .renameTo(new File(SDCardUtils
                                                        .getMediaCachePath(),
                                                        finalMediaName));
                                        if (renameResult) {
                                            Log.d(TAG,
                                                    "文章音频 "
                                                            + finalMediaFile
                                                            .getName()
                                                            + " 重命名成功 ");
                                        }
                                    } else {
                                        Log.e(TAG,
                                                "文章音频 "
                                                        + finalMediaFile
                                                        .getName()
                                                        + " 下载失败");
                                    }
                                }
                            });
                        }

                    }
                }
            }
        }

    }

    /**
     * 删除过期的声音文件
     */
    private void deleteOutOfDataMediaFile() {
        File mediaFileDir = new File(SDCardUtils.getMediaCachePath());
        File[] files = mediaFileDir.listFiles();
        if (files != null && files.length > 0) {
            String mediaName = null;
            String fileName = null;
            Map<String, File> fileMap = new HashMap<String, File>();
            File f = null;
            for (File file : files) {
                fileName = file.getName();

                mediaName = fileName
                        .substring(0, fileName.lastIndexOf("-") + 3);
                fileMap.put(mediaName, file);

            }
            if (fileMap.keySet().size() > 0) {
                TreeSet<String> ts = new TreeSet<String>(new Comparator<String>() {

                    @Override
                    public int compare(String lhs, String rhs) {
                        return rhs.compareTo(lhs);
                    }
                });
                ts.addAll(fileMap.keySet());

                Iterator<String> iterator = ts.iterator();
                int count = 1;
                while (iterator.hasNext()) {
                    String name = iterator.next();

                    if (count > 10) {
                        f = fileMap.get(name);
                        boolean r = f.delete();
                        if (r) {
                            Log.d(TAG, "删除文件  " + name + " 成功");
                        }
                    }
                    count++;

                }
            }

        }
    }

    private File getCacheMediaFile(String fileName) {
        // 文件名规则 文章名-md5.mp3.hh
        File mediaFile = new File(SDCardUtils.getMediaCachePath(), fileName);
        return mediaFile;
    }

    public static String getFileMd5(String path) {
        String checksum = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Using MessageDigest update() method to provide input
            byte[] buffer = new byte[8192];
            int numOfBytesRead;
            while ((numOfBytesRead = fis.read(buffer)) > 0) {
                md.update(buffer, 0, numOfBytesRead);
            }
            byte[] hash = md.digest();
            checksum = new BigInteger(1, hash).toString(16); // don't use this,
            // truncates
            // leading zero
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return checksum;
    }

    /**
     * 返回指定的文章音频是否已经有缓存
     */
    public boolean isCacheOk(Article article) {
        return false;
    }

    /**
     * 获取某文章音频的缓存地址，如果没有缓存或者缓存未完成则返回空
     */
    public String getCachePath(Article article) {
        return null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
