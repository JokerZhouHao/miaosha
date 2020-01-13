package com.imooc.miaosha.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.miaosha.domain.User;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.UserService;

@Controller
@RequestMapping("/demo")
public class SampleController {
	
	@Autowired
	UserService userService;
	
	@RequestMapping("/")
	@ResponseBody
	public String home() {
		System.out.println("135");
		return "Hello World!";
	}
	
	//1. rest api json输出2页面
	@RequestMapping("/hello")
	@ResponseBody
	public Result<String> hello() {
		System.out.println("1212");
		return Result.sucess("hello, imooc");
	}
	
	@RequestMapping("/helloError")
	@ResponseBody
	public Result<String> helloError() {
		return Result.error(CodeMsg.SERVER_ERROR);
	}
	
	@RequestMapping("/thymeleaf")
	public String thymeleaf(Model md) {
		md.addAttribute("name", "zhouhao");
		return "Hello";
	}
	
	@RequestMapping("/db/tx")
	@ResponseBody
	public Result<Boolean> dbTx(Model md) {
		return Result.sucess(userService.tx());
	}

}
