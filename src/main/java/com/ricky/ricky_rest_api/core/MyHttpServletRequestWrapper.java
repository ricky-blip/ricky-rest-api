package com.ricky.ricky_rest_api.core;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.*;

/**
 Data stream servlet saat sudah dipanggil akan hilang
 prose override pada class ini untuk membuat object penampung data nya
 dengan tujuan agar data nya dapat digunakan di class lain.
 */
public class MyHttpServletRequestWrapper extends HttpServletRequestWrapper {
	private final String body;public MyHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
			InputStream inputStream = request.getInputStream();

			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

				char[] charBuffer = new char[128];
				int bytesRead = -1;

				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (IOException ex) {
			System.out.println("Error reading the request body...");
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					System.out.println("Error closing bufferedReader...");
				}
			}
		}
		body = stringBuilder.toString();
	}

	@Override
	public ServletInputStream getInputStream () {
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes());

		ServletInputStream inputStream = new ServletInputStream() {
			@Override
			public boolean isFinished() {
				return false;
			}

			@Override
			public boolean isReady() {
				return false;
			}

			@Override
			public void setReadListener(ReadListener readListener) {

			}

			public int read () throws IOException {
				return byteArrayInputStream.read();
			}
		};
		return inputStream;
	}

	@Override
	public BufferedReader getReader(){
		return new BufferedReader(new InputStreamReader(this.getInputStream()));
	}
}
