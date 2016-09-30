package com.lib.connect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface SocketPool {
	/**
	 * 使用 socket 发送数据并返回数据，此方法内发送请求 会阻塞线程
	 * 
	 * @return
	 */
	void getData(String host, int port, InputStream request, OutputStream response) throws IOException;
}
