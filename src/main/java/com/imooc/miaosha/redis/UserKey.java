package com.imooc.miaosha.redis;

public class UserKey extends BasePrefix {

	private UserKey(String prefix) {
		super(prefix);
	}
	
	private UserKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}
	
	public static UserKey token = new UserKey("tk");
	public static UserKey getById = new UserKey(0, "id");
//	public static UserKey getByName = new UserKey("name");
}
