package hear.app.helper;

import java.io.DataInputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class HttpActionUtil {

	private static final String TAG = "HttpActionUtil";

	public synchronized static boolean download(String fileUrl, File savePath)/* fileUrl网络资源地址 */
	{

		try {
			URL url = new URL(fileUrl);/* 将网络资源地址传给,即赋值给url */
			/* 此为联系获得网络资源的固定格式用法，以便后面的in变量获得url截取网络资源的输入流 */
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setConnectTimeout(20 * 1000);
			connection.setReadTimeout(20 * 1000);
			long startPosition = 0;
			if (savePath.exists() && savePath.length() > 0) {
				startPosition = savePath.length();
			} else {
				savePath.createNewFile();
			}
			connection.setRequestProperty("Range", "bytes=" + startPosition
					+ "-");
			DataInputStream in = new DataInputStream(
					connection.getInputStream());

			RandomAccessFile out = new RandomAccessFile(savePath, "rw");
			out.seek(startPosition);
			/* 将参数savePath，即将截取的图片的存储在本地地址赋值给out输出流所指定的地址 */
			byte[] buffer = new byte[4096];
			int count = 0;

			while ((count = in.read(buffer)) > 0)/* 将输入流以字节的形式读取并写入buffer中 */
			{
				out.write(buffer, 0, count);

			}
			out.close();/* 后面三行为关闭输入输出流以及网络资源的固定格式 */
			in.close();
			connection.disconnect();

			return true;/* 网络资源截取并存储本地成功返回true */

		} catch (Exception e) {
			Log.e(TAG, fileUrl + ",local path: " + savePath, e);
			return false;
		}
	}

}
