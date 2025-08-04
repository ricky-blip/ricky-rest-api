// File: util/ApiResponse.java
package com.ricky.ricky_rest_api.util;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonPropertyOrder({"meta", "data"})
public class ApiResponse<T> {
	private Meta meta;
	private T data;

	public ApiResponse(int code, String status, String message, T data) {
		this.meta = new Meta(code, status, message);
		this.data = data;
	}

	public Meta getMeta() { return meta; }
	public T getData() { return data; }

	public static class Meta {
		private int code;
		private String status;
		private String message;
		private String timestamp;

		public Meta(int code, String status, String message) {
			this.code = code;
			this.status = status;
			this.message = message;
			this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
		}

		// Getter
		public int getCode() { return code; }
		public String getStatus() { return status; }
		public String getMessage() { return message; }
		public String getTimestamp() { return timestamp; }
	}
}