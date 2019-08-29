package com.atgg.gmall.service;

import com.atgg.gmall.been.CartInfo;

import java.util.List;

public interface CartService {
    //添加购物项
    void addToCart(String skuId, String userId, int i);
    //获取所有购物项
    List<CartInfo> getCartList(String userId);
    //合并购物项
    List<CartInfo> mergeToCartList(List<CartInfo> cartListCookie, String userId);
}
