package com.imooc.miaosha.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.imooc.miaosha.domain.Goods;
import com.imooc.miaosha.domain.MiaoshaGoods;
import com.imooc.miaosha.domain.User;
import com.imooc.miaosha.vo.GoodsVo;

@Mapper
public interface GoodsDao {
	@Select("select g.*, mg.miaosha_price, mg.stock_count, mg.start_date, mg.end_date "
			+ "from miaosha_goods mg left join goods g on mg.goods_id=g.id")
	public List<GoodsVo> listGoodsVo();
	
	@Select("select g.*, mg.miaosha_price, mg.stock_count, mg.start_date, mg.end_date "
			+ "from miaosha_goods mg left join goods g on mg.goods_id=g.id "
			+ "where mg.goods_id=#{goodsId}")
	public GoodsVo getGoodsVoByGoodsId(@Param("goodsId") long goodsId);
	
	@Update("update miaosha_goods set stock_count = stock_count-1 where goods_id = #{goodsId}")
	public void reduceStock(MiaoshaGoods g);
}
