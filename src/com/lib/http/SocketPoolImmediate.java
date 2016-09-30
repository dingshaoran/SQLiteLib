package com.lib.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.lib.connect.SocketPool;

public class SocketPoolImmediate implements SocketPool {
	private final int mCacheCount;
	private final List<Socket> mIdleList = Collections.synchronizedList(new ArrayList<Socket>());
	private final int mConnectTimeout;
	private final int mReadTimeout;
	private static final int DEFAULT_CACHECOUNT = 3;
	private static final int DEFAULT_CONNECT_TIMEOUT = 15000;
	private static final int DEFAULT_READ_TIMEOUT = 30000;

	public SocketPoolImmediate(int cacheCount, int connectTimeout, int readTimeout) {
		this.mCacheCount = cacheCount;
		this.mConnectTimeout = connectTimeout;
		this.mReadTimeout = readTimeout;
	}

	public SocketPoolImmediate() {
		mCacheCount = DEFAULT_CACHECOUNT;
		mConnectTimeout = DEFAULT_CONNECT_TIMEOUT;
		mReadTimeout = DEFAULT_READ_TIMEOUT;
	}

	@Override
	public void getData(String host, int port, InputStream request, OutputStream response) throws IOException {
		Socket socket = getIdleSocket();
		socket.connect(new InetSocketAddress(host, port), mConnectTimeout);
		OutputStream outputStream = socket.getOutputStream();
		byte[] buffer = new byte[1024 * 100];
		while (request.read(buffer) != -1) {
			outputStream.write(buffer);
		}
		InputStream ins = socket.getInputStream();
		while (ins.read(buffer) != -1) {
			response.write(buffer);
		}
		outputStream.close();
		ins.close();
		setIdelSocket(socket);
	}

	private Socket getIdleSocket() throws SocketException {
		int size = mIdleList.size();
		if (size == 0) {
			Socket socket = new Socket();
			socket.setKeepAlive(true);
			socket.setSoTimeout(mReadTimeout);
			return socket;
		} else {
			return mIdleList.remove(size - 1);
		}
	}

	private void setIdelSocket(Socket socket) throws IOException {
		if (mIdleList.size() < mCacheCount) {
			mIdleList.add(socket);
		} else {
			socket.close();
		}
	}

}
