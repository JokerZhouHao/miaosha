package com.imooc.miaosha.controller;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.miaosha.access.AccessLimit;
import com.imooc.miaosha.domain.MiaoshaOrder;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.domain.OrderInfo;
import com.imooc.miaosha.domain.User;
import com.imooc.miaosha.rabbitmq.MQSender;
import com.imooc.miaosha.rabbitmq.MiaoshaMessage;
import com.imooc.miaosha.redis.AccessKey;
import com.imooc.miaosha.redis.GoodsKey;
import com.imooc.miaosha.redis.MiaoshaKey;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.redis.UserKey;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.GoodsService;
import com.imooc.miaosha.service.MiaoshaService;
import com.imooc.miaosha.service.MiaoshaUserService;
import com.imooc.miaosha.service.OrderService;
import com.imooc.miaosha.service.UserService;
import com.imooc.miaosha.util.MD5Util;
import com.imooc.miaosha.util.UUIDUtil;
import com.imooc.miaosha.util.ValidatorUtil;
import com.imooc.miaosha.vo.GoodsVo;
import com.imooc.miaosha.vo.LoginVo;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean{
	
	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	MiaoshaService miaoshaService;
	
	@Autowired
	MQSender sender;
	
	private Map<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();
	
	/**
	 * 系统初始化
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> goodslList = goodsService.listGoodsVo();
		if(goodslList == null) {
			return;
		}
		for(GoodsVo goods : goodslList) {
			localOverMap.put(goods.getId(), false);
			redisService.set(GoodsKey.getMiaoshaGoodsStock, "" + goods.getId(), goods.getStockCount());
		}
	}

	/**
	 * GET POST有什么区别
	 * GET 幂等 从服务端获取数据，不会对服务端数据产生影响
	 * POST  会对服务端数据产生影响
	 * 
	 */
	@RequestMapping(value ="/{path}/do_miaosha", method = RequestMethod.POST)
	@ResponseBody
	public Result<Integer> miaosha(Model model, MiaoshaUser user,
			@RequestParam("goodsId") long goodsId,
			@PathVariable("path") String path) {
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		
		// 验证path
		boolean check = miaoshaService.checkPath(user, goodsId, path);
		if(!check) {
			return Result.error(CodeMsg.REQUEST_ILLEGAL);
		}
		
		// 内存标记，减少redis访问
		boolean over = localOverMap.get(goodsId);
		if(over) {
			return Result.error(CodeMsg.MIAO_SHA_OVER);
		}
		
		// 预减库存
		Long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
		if(stock < 0) {
			localOverMap.put(goodsId, true);
			return Result.error(CodeMsg.MIAO_SHA_OVER);
		}
		
		// 是否重复秒杀
		boolean hasOrder = miaoshaService.hasMiaoshUserGood(user.getId(), goodsId);
		if(hasOrder) {
			redisService.incr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
			return Result.error(CodeMsg.REPEATE_MIAOSHA);
		}
		
		// 入队
		MiaoshaMessage message = new MiaoshaMessage();
		message.setUser(user);
		message.setGoodsId(goodsId);
		sender.sendMiaoshaMessage(message);
		return Result.sucess(0); // 0 排队中
		
		/*
		// 判断库存
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);  // 10个商品，请求同时到达
		int stock = goods.getStockCount();
		if(stock <= 0) {
			return Result.error(CodeMsg.MIAO_SHA_OVER);
		}
		
		// 判断是否已经秒杀到了
		MiaoshaOrder order = orderService.getMiaoShaOrderByUserIdGoodsId(user.getId(), goodsId);
		if(order != null) {
			return Result.error(CodeMsg.REPEATE_MIAOSHA);
		}
		
		// 减库存 下订单 写入秒杀订单
		OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
		return Result.sucess(orderInfo);
		*/
	}
	
	/**
	 * orderId: 成功
	 * -1 : 秒杀失败
	 * 0 : 排队中
	 */
	@RequestMapping(value ="/result", method = RequestMethod.GET)
	@ResponseBody
	public Result<Long> miaoshaResult(Model model, MiaoshaUser user,
			@RequestParam("goodsId") long goodsId) {
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		
		long result = miaoshaService.getMiaoshaResult(user.getId(), goodsId);
		return Result.sucess(result);
	}
	
	@AccessLimit(seconds=5, maxCount=5, needLogin=true)
	@RequestMapping(value ="/path", method = RequestMethod.GET)
	@ResponseBody
	public Result<String> getMiaoshaPath(HttpServletRequest request, MiaoshaUser user,
			@RequestParam("goodsId") long goodsId,
			@RequestParam(value="verifyCode", defaultValue="0") int verfiyCode) {
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		
		// 验证验证码
		boolean check = miaoshaService.checkVerifyCode(user, goodsId, verfiyCode);
		if(!check) {
			return Result.error(CodeMsg.REQUEST_ILLEGAL);
		}
		
		String path = miaoshaService.createMiaoshaPath(user, goodsId);
		return Result.sucess(path);
	}
	
	@RequestMapping(value ="/verifyCode", method = RequestMethod.GET)
	@ResponseBody
	public Result<String> getMiaoshaVerifyCode(HttpServletResponse response, Model model, MiaoshaUser user,
			@RequestParam("goodsId") long goodsId) {
		model.addAttribute("user", user);
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		
		BufferedImage image = miaoshaService.createVerifyCode(user, goodsId);
		try {
			OutputStream out = response.getOutputStream();
			ImageIO.write(image, "JPEG", out);
			out.flush();
			out.close();
			return null;
		} catch(Exception e) {
			e.printStackTrace();
			return Result.error(CodeMsg.MIAOSHA_FAIL);
		}
	}
	
}
