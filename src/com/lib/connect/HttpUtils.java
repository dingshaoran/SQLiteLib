package com.lib.connect;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import android.util.Log;

/**
 * 
 * @author dingxiaoyu
 * 
 * @date 2013-5-24
 */
public class HttpUtils {

	public static String sendGetRequest(String url) throws Exception {
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				10 * 1000);
		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 10 * 1000);
		HttpResponse response = httpClient.execute(get);
		if (response.getStatusLine().getStatusCode() == 200) {
			String result = EntityUtils.toString(response.getEntity());
			return result;
		}
		return null;
	}

	@SuppressWarnings("resource")
	public static String sendMultipartPostRequest(String url,
			Map<String, Object> params, FormFile file) throws Exception {
		final String BOUNDARY = "---------------------------7da2137580612"; // 数据分隔线
		final String endline = "--" + BOUNDARY + "--\r\n";// 数据结束标志

		int fileDataLength = 0;

		StringBuilder fileExplain = new StringBuilder();
		fileExplain.append("--");
		fileExplain.append(BOUNDARY);
		fileExplain.append("\r\n");
		fileExplain.append("Content-Disposition: form-data;name=\""
				+ file.getParameterName() + "\";filename=\""
				+ file.getFilname() + "\"\r\n");
		fileExplain.append("Content-Type: " + file.getContentType()
				+ "\r\n\r\n");
		fileExplain.append("\r\n");
		fileDataLength += fileExplain.length();
		if (file.getInStream() != null) {
			fileDataLength += file.getFile().length();
		} else {
			fileDataLength += file.getData().length;
		}

		StringBuilder textEntity = new StringBuilder();
		for (Map.Entry<String, Object> entry : params.entrySet()) {// 构造文本类型参数的实体数据
			textEntity.append("--");
			textEntity.append(BOUNDARY);
			textEntity.append("\r\n");
			textEntity.append("Content-Disposition: form-data; name=\""
					+ entry.getKey() + "\"\r\n\r\n");
			textEntity.append(entry.getValue());
			textEntity.append("\r\n");
		}
		// 计算传输给服务器的实体数据总长度
		int dataLength = textEntity.toString().getBytes().length
				+ fileDataLength + endline.getBytes().length;
		String result = "err";
		// try {
		URL _url = new URL(url);
		int port = _url.getPort() == -1 ? 80 : _url.getPort();
		Socket socket = new Socket(InetAddress.getByName(_url.getHost()), port);
		socket.setKeepAlive(true);
		socket.setSoTimeout(5 * 1000);

		OutputStream outStream = socket.getOutputStream();
		// 下面完成HTTP请求头的发送
		String requestmethod = "POST " + _url.getPath() + " HTTP/1.1\r\n";
		outStream.write(requestmethod.getBytes());
		String accept = "Accept: image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*\r\n";
		outStream.write(accept.getBytes());
		String language = "Accept-Language: zh-CN\r\n";
		outStream.write(language.getBytes());
		String contenttype = "Content-Type: multipart/form-data; boundary="
				+ BOUNDARY + "\r\n";
		outStream.write(contenttype.getBytes());
		String contentlength = "Content-Length: " + dataLength + "\r\n";
		outStream.write(contentlength.getBytes());
		String alive = "Connection: Keep-Alive\r\n";
		outStream.write(alive.getBytes());
		String host = "Host: " + _url.getHost() + ":" + port + "\r\n";
		outStream.write(host.getBytes());
		// 写完HTTP请求头后根据HTTP协议再写一个回车换行
		outStream.write("\r\n".getBytes());
		// 把所有文本类型的实体数据发送出来
		outStream.write(textEntity.toString().getBytes());
		// 把所有文件类型的实体数据发送出来
		StringBuilder fileEntity = new StringBuilder();
		fileEntity.append("--");
		fileEntity.append(BOUNDARY);
		fileEntity.append("\r\n");
		fileEntity.append("Content-Disposition: form-data;name=\""
				+ file.getParameterName() + "\";filename=\""
				+ file.getFilname() + "\"\r\n");
		fileEntity
				.append("Content-Type: " + file.getContentType() + "\r\n\r\n");
		outStream.write(fileEntity.toString().getBytes());
		if (file.getInStream() != null) {
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = file.getInStream().read(buffer, 0, 1024)) != -1) {

				outStream.write(buffer, 0, len);
			}
			file.getInStream().close();
		} else {
			outStream.write(file.getData(), 0, file.getData().length);
		}
		outStream.write("\r\n".getBytes());
		// 下面发送数据结束标志，表示数据已经结束
		outStream.write(endline.getBytes());

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));

		if (reader.readLine().indexOf("200") == -1) {//
			// 读取web服务器返回的数据，判断请求码是否为200，如果不是200，代表请求失败
			return null;
		}
		char[] chars = new char[1024];
		reader.read(chars);
		result = new String(chars);
		int begin = result.indexOf('{');
		int end = result.indexOf('}') + 1;
		result = result.substring(begin, end);
		outStream.flush();
		outStream.close();
		reader.close();
		socket.close();
		// } catch (SocketTimeoutException e) {
		// System.out.println(e);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		return result;
	}

	@SuppressWarnings("resource")
	public static String sendMultipartPostRequest(String url,
			Map<String, Object> params, FormFile file, final HttpProgressCallback callback) throws Exception {
		final String BOUNDARY = "---------------------------7da2137580612"; // 数据分隔线
		final String endline = "--" + BOUNDARY + "--\r\n";// 数据结束标志

		int fileDataLength = 0;

		StringBuilder fileExplain = new StringBuilder();
		fileExplain.append("--");
		fileExplain.append(BOUNDARY);
		fileExplain.append("\r\n");
		fileExplain.append("Content-Disposition: form-data;name=\""
				+ file.getParameterName() + "\";filename=\""
				+ file.getFilname() + "\"\r\n");
		fileExplain.append("Content-Type: " + file.getContentType()
				+ "\r\n\r\n");
		fileExplain.append("\r\n");
		fileDataLength += fileExplain.length();
		if (file.getInStream() != null) {
			fileDataLength += file.getFile().length();
		} else {
			fileDataLength += file.getData().length;
		}
ThreadFactory
		StringBuilder textEntity = new StringBuilder();
		for (Map.Entry<String, Object> entry : params.entrySet()) {// 构造文本类型参数的实体数据
			textEntity.append("--");
			textEntity.append(BOUNDARY);
			textEntity.append("\r\n");
			textEntity.append("Content-Disposition: form-data; name=\""
					+ entry.getKey() + "\"\r\n\r\n");
			textEntity.append(entry.getValue());
			textEntity.append("\r\n");
		}
		 
		// 计算传输给服务器的实体数据总长度
		int dataLength = textEntity.toString().getBytes().length
				+ fileDataLength + endline.getBytes().length;
		String result = "err";
		// try {
		URL _url = new URL(url);
		int port = _url.getPort() == -1 ? 80 : _url.getPort();
		Socket socket = new Socket(InetAddress.getByName(_url.getHost()), port);
		socket.setKeepAlive(true);
		socket.setSoTimeout(5 * 1000);

		OutputStream outStream = socket.getOutputStream();
		// 下面完成HTTP请求头的发送
		String requestmethod = "POST " + _url.getPath() + " HTTP/1.1\r\n";
		outStream.write(requestmethod.getBytes());
		String accept = "Accept: image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*\r\n";
		outStream.write(accept.getBytes());
		String language = "Accept-Language: zh-CN\r\n";
		outStream.write(language.getBytes());
		String contenttype = "Content-Type: multipart/form-data; boundary="
				+ BOUNDARY + "\r\n";
		outStream.write(contenttype.getBytes());
		String contentlength = "Content-Length: " + dataLength + "\r\n";
		outStream.write(contentlength.getBytes());
		String alive = "Connection: Keep-Alive\r\n";
		outStream.write(alive.getBytes());
		String host = "Host: " + _url.getHost() + ":" + port + "\r\n";
		outStream.write(host.getBytes());
		// 写完HTTP请求头后根据HTTP协议再写一个回车换行
		outStream.write("\r\n".getBytes());
		// 把所有文本类型的实体数据发送出来
		outStream.write(textEntity.toString().getBytes());
		// 把所有文件类型的实体数据发送出来
		StringBuilder fileEntity = new StringBuilder();
		fileEntity.append("--");
		fileEntity.append(BOUNDARY);
		fileEntity.append("\r\n");
		fileEntity.append("Content-Disposition: form-data;name=\""
				+ file.getParameterName() + "\";filename=\""
				+ file.getFilname() + "\"\r\n");
		fileEntity
				.append("Content-Type: " + file.getContentType() + "\r\n\r\n");
		outStream.write(fileEntity.toString().getBytes());
		if (file.getInStream() != null) {
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = file.getInStream().read(buffer, 0, 1024)) != -1) {
				callback.uploadProgress(1024 / ((float) fileDataLength / (float) 100));
				outStream.write(buffer, 0, len);
			}
			file.getInStream().close();
		} else {
			outStream.write(file.getData(), 0, file.getData().length);
		}
		outStream.write("\r\n".getBytes());
		// 下面发送数据结束标志，表示数据已经结束
		outStream.write(endline.getBytes());

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));

		if (reader.readLine().indexOf("200") == -1) {//
			// 读取web服务器返回的数据，判断请求码是否为200，如果不是200，代表请求失败
			return null;
		}
		char[] chars = new char[1024];
		reader.read(chars);
		result = new String(chars);
		int begin = result.indexOf('{');
		int end = result.indexOf('}') + 1;
		result = result.substring(begin, end);
		outStream.flush();
		outStream.close();
		reader.close();
		socket.close();
		// } catch (SocketTimeoutException e) {
		// System.out.println(e);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		return result;
	}

	public static String sendSimplePostRequest(String url,
			Map<String, String> params) throws Exception {
		HttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				5 * 1000);
		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 5 * 1000);
		HttpPost post = new HttpPost(url);
		List<NameValuePair> paramList = new ArrayList<NameValuePair>();
		for (String key : params.keySet()) {
			paramList.add(new BasicNameValuePair(key, params.get(key)
					.toString()));
		}
		post.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));
		HttpResponse response = httpClient.execute(post);
		if (response.getStatusLine().getStatusCode() == 200) {
			String result = EntityUtils.toString(response.getEntity(), "UTF-8");
			return result;
		}
		return null;
	}

	/**
	 * HttpPost
	 * 
	 * @param url
	 *            上传路径
	 * @param params
	 *            上传参数
	 * @return
	 */
	public static String SendPost(String url, Map<String, Object> parm) {
		String result = "";
		String urlparm = "";
		String parameter = "";

		URL u = null;
		HttpURLConnection con = null;
		// 构建请求参数
		StringBuffer sb = new StringBuffer();
		if (parm != null) {
			for (Entry<String, Object> e : parm.entrySet()) {
				sb.append(e.getKey());
				sb.append("=");
				sb.append(e.getValue());
				sb.append("&");
			}
			sb.substring(0, sb.length() - 1);
		}
		// 尝试发送请求
		try {
			u = new URL(url + parameter + urlparm);
			Log.e("post", u + "--" + "parm:" + sb.toString());
			con = (HttpURLConnection) u.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setConnectTimeout(1000 * 30);
			con.setReadTimeout(1000 * 30);

			con.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			OutputStreamWriter osw = new OutputStreamWriter(
					con.getOutputStream(), "UTF-8");
			osw.write(sb.toString());
			osw.flush();
			osw.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
			in.close();
		} catch (Exception e) {
			if (e.toString().contains("SocketTimeoutException"))
				result = "timedout";
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
		return result;
	}

}
