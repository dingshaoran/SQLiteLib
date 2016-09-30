package com.lib.http;

public class RequestLine {
	private final String header;

	private RequestLine(Builder builder) {
		if (builder.methord == null) {
			throw new NullPointerException("请设置那种请求类型");
		}
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(builder.methord)
				.append(" ")
				.append(builder.uri == null ? "" : builder.uri)
				.append(" ")
				.append("HTTP/")
				.append(builder.version == null ? "1.1" : builder.version);
		header = stringBuilder.toString();
	}

	public String getHeader() {
		return header;
	}

	public static class Builder {
		private String methord;
		private String uri;
		private String version;

		public String getMethord() {
			return methord;
		}

		public String getUri() {
			return uri;
		}

		public String getVersion() {
			return version;
		}

		public void setMethord(String methord) {
			this.methord = methord;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public RequestLine build() {
			return new RequestLine(this);
		}

	}
}
