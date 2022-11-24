package com.smartdoc.demo.project.api.constants;

public class ResponseMessage<T> {

	/**
	 * 响应编码
	 */
	private int code;
	/**
	 * 数据实体
	 */
	private T t;
	/**
	 * 描述信息
	 */
	private String message;

	public ResponseMessage(int code, T t, String message) {
		this.code = code;
		this.t = t;
		this.message = message;
	}


	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public T getT() {
		return t;
	}

	public void setT(T t) {
		this.t = t;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public static <T> ResponseMessage<T> success(T t) {
		return new ResponseMessage<>(200, t, "SUCCESS");
	}

	public static <T> ResponseMessage<T> error(T t) {
		return new ResponseMessage<>(500, t, "Error");
	}
}
