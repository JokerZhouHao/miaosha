package com.imooc.miaosha.redis;

public abstract class BasePrefix implements KeyPrefix{
	
	private int expireSeconds = 0;
	private String prefix;
	
	public BasePrefix(int expireSeconds, String prefix) {
		super();
		this.expireSeconds = expireSeconds;
		this.prefix = prefix;
	}
	
	public BasePrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public int expireSeconds() {	// 默认0代表永不过期
		return expireSeconds;
	}

	@Override
	public String getPrefix() {
		String className = getClass().getSimpleName();
		return className + ":" + prefix;
	}
	
}
