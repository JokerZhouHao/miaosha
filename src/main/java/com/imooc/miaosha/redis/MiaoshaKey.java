package com.imooc.miaosha.redis;

public class MiaoshaKey extends BasePrefix {

	private MiaoshaKey(String prefix) {
		super(prefix);
	}
	
	private MiaoshaKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}
	
	public static MiaoshaKey isGoodsOver = new MiaoshaKey("go");
	public static MiaoshaKey getMiaoshUserGood = new MiaoshaKey("ug");
}
