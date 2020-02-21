package com.imooc.miaosha.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.imooc.miaosha.domain.MiaoshaUser;

@Mapper
public interface MiaoshaUserDao {
	
	@Select("select * from miaosha_user where id=#{id}")
	public MiaoshaUser getById(long id);
	
	@Update("update miaosha_user set password=#{password} where id=#{id}")
	public void update(MiaoshaUser toBeUpdate);
	
	@Insert("insert into miaosha_user(id, password, salt, register_date, nickname)"
			+ " values(#{id}, #{password}, #{salt}, #{registerDate}, 'test')")
	public int insert(MiaoshaUser user);
}
