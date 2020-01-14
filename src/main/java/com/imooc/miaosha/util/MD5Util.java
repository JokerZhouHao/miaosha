package com.imooc.miaosha.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {
	public static String md5(String src) {
		return DigestUtils.md5Hex(src);
	}
	
	private static final String salt = "1a2b3c4d";
	
	public static String inputPassToFormPass(String inputPass) {
		return md5("" + salt.charAt(0) + salt.charAt(2) + inputPass + salt.charAt(5) + salt.charAt(4));
	}
	
	public static String formPassToDBPass(String formPass, String salt) {
		return md5("" + salt.charAt(0) + salt.charAt(2) + formPass + salt.charAt(5) + salt.charAt(4));
	}
	
	public static String inputPassToDBPass(String inputPass, String saltDB) {
		return formPassToDBPass(inputPassToFormPass(inputPass), saltDB);
	}
	
	public static void main(String[] args) {
		System.out.println(inputPassToFormPass("123456"));
		System.out.println(formPassToDBPass("d3b1294a61a07da9b49b6e22b2cbd7f9", "1a2b3c4d"));
		System.out.println(inputPassToDBPass("123456", "1a2b3c4d"));
	}
	
}
