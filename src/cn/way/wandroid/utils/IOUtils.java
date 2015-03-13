package cn.way.wandroid.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

public class IOUtils {
	
	/**
	 * 将输入流写入输出流
	 * @param in
	 * @param out
	 * @param bufferSize
	 * @throws IOException
	 * @author Wayne 2013-1-24
	 */
	public static void writeI2O(InputStream in, OutputStream out,int bufferSize)
			throws IOException {
		byte[] buf = new byte[bufferSize];
		int len = 0;
		while ((len = in.read(buf)) != -1) {
			out.write(buf, 0, len);
			out.flush();
		}
		try {
			if (out != null)
				out.close();
		} finally {
			if (in != null)
				in.close();
		}
	}
	
	public static boolean isExternalStorageCanReadWrite(){
		return (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) ;
	}
	
	public static File getExternalStorageShareImageDirectory(){
		File dir = new File(Environment.getExternalStorageDirectory(),"Pictures");
		if (!dir.exists()) {
			dir.mkdirs();
		};
		return dir;
	}
	
	public static  boolean saveDataToExternalStroageShareImageDirectory(Context context,byte[] data,String filename){
		boolean result = false;
		if (filename==null) {
			filename = UUID.randomUUID().toString()+".jpg";
		}
		if (isExternalStorageCanReadWrite()) {
			File subDir = new File(getExternalStorageShareImageDirectory().getAbsolutePath());
			if (!subDir.exists()) {
				subDir.mkdirs();
			}
			File toFile = new File(subDir, filename);
			result =  saveData(data,toFile );
			if (result) {
				saveImageToPhotoAlbum(context, toFile.getAbsolutePath());
			}
		}
		return result;
	}
	
	public static void saveImageToPhotoAlbum(Context context,String urlString){
		ContentValues cv = new ContentValues();
		cv.put("_data", urlString);
		cv.put("mime_type", "image/jpeg");
		ContentResolver cr = context.getContentResolver();
		Uri localUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		cr.insert(localUri, cv);
	}
	
	public static boolean saveData(byte[] data,File tofile){
		boolean result = false;
		if (data!=null) {
			try {
				writeI2O(new ByteArrayInputStream(data),new FileOutputStream(tofile), 1024*8);
				result = true;
			} catch (IOException e) {}
		}
		return result;
	}
	
	/**
	 * 将InputStream转换成String
	 * 
	 * @param in
	 *            InputStream
	 * @return String
	 * @throws Exception
	 * 
	 */
	public static String readString(InputStream in){
		return readString(in, "UTF-8");
	}

	/**
	 * 将InputStream转换成某种字符编码的String
	 * 
	 * @param in
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public static String readString(InputStream in, String charsetName) {
		byte[] bytes =  inputStreamToByteArray(in);
		if (bytes!=null) {
			try {
				return new String(bytes, charsetName);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}


	/**
	 * 将InputStream转换成byte数组
	 * 
	 * @param in
	 *            InputStream
	 * @return byte[]
	 * @throws IOException
	 */
	public static byte[] inputStreamToByteArray(InputStream in) {
		if (in==null) {
			return null;
		}
		ByteArrayOutputStream byteArrayOs = new ByteArrayOutputStream();
		try {
			writeI2O(in, byteArrayOs, 1024);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return byteArrayOs.toByteArray();
	}
}
