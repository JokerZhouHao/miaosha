package com.imooc.miaosha.result;

public class Result<T> {
	private int code;
	private String msg;
	private T data;
	
	private Result(T data) {
		super();
		this.code = 0;
		this.msg = "success";
		this.data = data;
	}

	private Result(CodeMsg cm) {
		if(cm == null)	return;
		this.code = cm.getCode();
		this.msg = cm.getMsg();
	}

	/**
	 * 成功时调用
	 * @return
	 */
	public static <T> Result<T> sucess(T data) {
		return new Result<T>(data);
	}
	
	/**
	 * 失败时调用
	 * @return
	 */
	public static <T> Result<T> error(CodeMsg cm) {
		return new Result<T>(cm);
	}
	
	
	public int getCode() {
		return code;
	}
	public String getMsg() {
		return msg;
	}
	public T getData() {
		return data;
	}
}
