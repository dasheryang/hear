package hear.app.helper;

import java.io.File;

import android.os.Environment;

public class SDCardUtils {
	private static android.os.StatFs statfs = new android.os.StatFs(
			android.os.Environment.getExternalStorageDirectory()
					.getAbsolutePath());

	public static final String NOT_SDCARD = "Please insert the memory card";

	public static String GetSysPath() {
		return android.os.Environment.getRootDirectory().getAbsolutePath();
	}

	public static boolean isSDCardEnable() {

		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	public static long getTotalSize() {
		long nTotalBlocks = statfs.getBlockCount();
		long nBlocSize = statfs.getBlockSize();
		long nSDTotalSize = nTotalBlocks * nBlocSize;
		return nSDTotalSize;
	}

	public static long getFreeSize() {

		long nBlocSize = statfs.getBlockSize();

		long nAvailaBlock = statfs.getAvailableBlocks();

		long nSDFreeSize = nAvailaBlock * nBlocSize;
		return nSDFreeSize;
	}

	public static String getSDcardPath() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			return sdcardDir.getPath();
		}
		return null;
	}

	public static String getSaveRootPath() {
		String path = getSDcardPath() + File.separator + ".hearheart";
		return path;
	}

	/**
	 * check sd card first
	 * 
	 * @param path
	 * @return null if no sdcard;
	 */
	static public File fileFromSDCard(String path) {
		if (path == null || "".equals(path))
			return null;
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return null;
		}

		return new File(path);
	}

	public static String getMediaCachePath() {
		return getSaveRootPath() + "/media/cache";
	}

	public static void makeDirs() {
		File dir = new File(SDCardUtils.getSaveRootPath());
		dir.mkdirs();
		dir = new File(SDCardUtils.getMediaCachePath());
		dir.mkdirs();

	}
}
