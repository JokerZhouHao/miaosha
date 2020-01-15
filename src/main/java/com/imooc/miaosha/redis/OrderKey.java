package com.imooc.miaosha.redis;

public class OrderKey extends BasePrefix {

	public OrderKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}
	
	public OrderKey(String prefix) {
		this(0, prefix);
	}
	
	public static OrderKey getMiaoshaOrderByUidGid = new OrderKey("moug");
	
}
