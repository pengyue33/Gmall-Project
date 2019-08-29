package com.atgg.gmall.cart.mapper;

import com.atgg.gmall.been.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo> {

    List<CartInfo> selectCartListWithCurPrice(String userId);
}
