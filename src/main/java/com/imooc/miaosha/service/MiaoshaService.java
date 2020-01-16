package com.imooc.miaosha.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imooc.miaosha.dao.GoodsDao;
import com.imooc.miaosha.domain.Goods;
import com.imooc.miaosha.domain.MiaoshaOrder;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.domain.OrderInfo;
import com.imooc.miaosha.redis.MiaoshaKey;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.util.MD5Util;
import com.imooc.miaosha.util.UUIDUtil;
import com.imooc.miaosha.vo.GoodsVo;

@Service
public class MiaoshaService {
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	RedisService redisService;
	
	@Transactional
	public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
		// 减库存 下订单 写入秒杀订单
		boolean success = goodsService.reduceStock(goods);
		// order_info miaosha_order
		if(success) {
			OrderInfo orderInfo = orderService.createOrder(user, goods);
			if(orderInfo != null) {
				setMiaoshUserGood(orderInfo.getUserId(), orderInfo.getGoodsId());
			}
			return orderInfo;
		} else {
			setGoodsOver(goods.getId());
			return null;
		}
	}

	public long getMiaoshaResult(Long userId, long goodsId) {
		MiaoshaOrder order = orderService.getMiaoShaOrderByUserIdGoodsId(userId, goodsId);
		if(order != null) { // 秒杀成功
			return order.getOrderId();
		} else {
			boolean isOver = getGoodsOver(goodsId);
			if(isOver) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	private void setGoodsOver(Long goodsId) {
		redisService.set(MiaoshaKey.isGoodsOver, "" + goodsId, true);
	}
	
	private boolean getGoodsOver(long goodsId) {
		return redisService.exists(MiaoshaKey.isGoodsOver, "" + goodsId);
	}
	
	private void setMiaoshUserGood(Long userId, Long goodsId) {
		redisService.incr(MiaoshaKey.getMiaoshaUserGood, "" + userId + "-" + goodsId);	
	}
	
	public boolean hasMiaoshUserGood(Long userId, Long goodsId) {
		return redisService.exists(MiaoshaKey.getMiaoshaUserGood, "" + userId + "-" + goodsId);
	}

	public String createMiaoshaPath(MiaoshaUser user, Long goodsId) {
		String str = MD5Util.md5(UUIDUtil.uuid()) + "123456";
		redisService.set(MiaoshaKey.getMiaoshaPath, "" + user.getId() + "_" + goodsId, str);
		return str;
	}

	public boolean checkPath(MiaoshaUser user, long goodsId, String path) {
		if(user == null || path == null) {
			return false;
		}
		String pathOld = redisService.get(MiaoshaKey.getMiaoshaPath, "" + user.getId() + "_" + goodsId, String.class);
		return path.equals(pathOld);
	}

	public BufferedImage createVerifyCode(MiaoshaUser user, long goodsId) {
		if(user == null || goodsId <= 0) {
			return null;
		}
		
		int width = 80;
		int height = 32;
		//create the image
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		// set the background color
		g.setColor(new Color(0xDCDCDC));
		g.fillRect(0, 0, width, height);
		// draw the border
		g.setColor(Color.black);
		g.drawRect(0, 0, width - 1, height - 1);
		// create a random instance to generate the codes
		Random rdm = new Random();
		// make some confusion
		for (int i = 0; i < 50; i++) {
			int x = rdm.nextInt(width);
			int y = rdm.nextInt(height);
			g.drawOval(x, y, 0, 0);
		}
		// generate a random code
		String verifyCode = generateVerifyCode(rdm);
		g.setColor(new Color(0, 100, 0));
		g.setFont(new Font("Candara", Font.BOLD, 24));
		g.drawString(verifyCode, 8, 24);
		g.dispose();
		//把验证码存到redis中
		int rnd = calc(verifyCode);
		redisService.set(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, rnd);
		//输出图片
		return image;
	}
	
	private int calc(String exp) {
		try {
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("JavaScript");
			Integer catch1 = (Integer)engine.eval(exp);
			return catch1.intValue();
		}catch(Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	private static char[] ops = new char[] {'+', '-', '*'};
	
	/**
	 * + - *
	 * @param rdm
	 * @return
	 */
	private String generateVerifyCode(Random rdm) {
		int num1 = rdm.nextInt(10);
		int num2 = rdm.nextInt(10);
		int num3 = rdm.nextInt(10);
		char op1 = ops[rdm.nextInt(3)];
		char op2 = ops[rdm.nextInt(3)];
		String exp = ""+ num1 + op1 + num2 + op2 + num3;
		return exp;
	}

	public boolean checkVerifyCode(MiaoshaUser user, long goodsId, int verfiyCode) {
		if(user == null || goodsId <= 0) {
			return false;
		}
		Integer codeOld = redisService.get(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, Integer.class);
		if(codeOld == null || codeOld - verfiyCode != 0) {
			return false;
		}
		redisService.delete(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId);
		return true;
	}
}
