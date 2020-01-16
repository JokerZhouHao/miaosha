package com.imooc.miaosha.service;

import java.util.List;

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
		redisService.incr(MiaoshaKey.getMiaoshUserGood, "" + userId + "-" + goodsId);	
	}
	
	public boolean hasMiaoshUserGood(Long userId, Long goodsId) {
		return redisService.exists(MiaoshaKey.getMiaoshUserGood, "" + userId + "-" + goodsId);
	}
}
