package com.imooc.miaosha.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.domain.User;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.redis.UserKey;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.GoodsService;
import com.imooc.miaosha.service.MiaoshaUserService;
import com.imooc.miaosha.service.UserService;
import com.imooc.miaosha.util.MD5Util;
import com.imooc.miaosha.util.ValidatorUtil;
import com.imooc.miaosha.vo.GoodsVo;
import com.imooc.miaosha.vo.LoginVo;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
	
	@RequestMapping("/info")
	@ResponseBody
	public Result<MiaoshaUser> info(Model model, MiaoshaUser user) {
		return Result.sucess(user);
	}
	
	@RequestMapping("/register")
	public String do_register() {
		return "register";
	}
	
	@RequestMapping("/do_register")
	@ResponseBody
	public Result<String> register(@Valid LoginVo loginVo) {
		MiaoshaUser user = userService.getById(Long.parseLong(loginVo.getMobile()));
		if(user != null) {
			return Result.error(CodeMsg.USER_HAS_EXIST);
		}
		user = new MiaoshaUser();
		user.setId(Long.parseLong(loginVo.getMobile()));
		user.setSalt(MD5Util.salt);
		user.setPassword(MD5Util.formPassToDBPass(loginVo.getPassword(), user.getSalt()));
		user.setRegisterDate(new Date());
		if(userService.addUser(user) == 1)	return Result.sucess("注册成功");
		else return Result.error(CodeMsg.SERVER_ERROR);
	}
}
