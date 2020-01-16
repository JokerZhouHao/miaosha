package com.imooc.miaosha.redis;

public class MiaoshaKey extends BasePrefix {

	private MiaoshaKey(String prefix) {
		super(prefix);
	}
	
	private MiaoshaKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}
	
	public static MiaoshaKey isGoodsOver = new MiaoshaKey("go");
	public static MiaoshaKey getMiaoshaUserGood = new MiaoshaKey("ug");
	public static MiaoshaKey getMiaoshaPath = new MiaoshaKey(60, "mp");
	public static MiaoshaKey getMiaoshaVerifyCode = new MiaoshaKey(300, "vc");
	
}
