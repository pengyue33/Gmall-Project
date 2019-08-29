package com.atgg.gmall.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atgg.gmall.been.CartInfo;
import com.atgg.gmall.been.SkuInfo;
import com.atgg.gmall.cart.handler.CartCookieHandler;
import com.atgg.gmall.config.CookieUtil;
import com.atgg.gmall.config.LoginRequire;
import com.atgg.gmall.service.CartService;
import com.atgg.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CartController {

    @Reference
    private ManageService manageService;
    @Reference
    private CartService cartService;
     @Autowired
    CartCookieHandler cartCookieHandler;
    /**
     * 添加购物项到购物车
     * @param request
     * @return
     */
      @RequestMapping("addToCart")
      @LoginRequire(autoRedirect = false)  //判断用户是否登录，拦截器
    public String addToCart(HttpServletRequest request, HttpServletResponse response){
          //获取skuId ,num
          String num = request.getParameter("num");
          String skuId = request.getParameter("skuId");
          //根据skuId 获取商品详情
          SkuInfo skuInfo = manageService.getSkuInfoBySkuId(skuId);
           //根据用户Id 判断用户是否登录
          String userId = (String) request.getAttribute("userId");
          if(userId!=null){
              //登录状态添加购物项 ==》mysql + redis
              cartService.addToCart(skuId,userId,Integer.parseInt(num));
          }else {
              //未登录状态添加购物项==》cookie  ---hash  {key,file,value}
              cartCookieHandler.addToCart(request,response,skuId,Integer.parseInt(num));
          }

          //将商品详情放入请求域中,去success显示
          request.setAttribute("skuInfo",skuInfo);
          //添加的商品数量
          request.setAttribute("skuNum",num);
          return  "success";
    }

    //购物车列表
     @RequestMapping("cartList")
     @LoginRequire(autoRedirect=false)
    public String cartList(HttpServletRequest request ,HttpServletResponse response){
         List<CartInfo> cartInfoList = new ArrayList<>();
          //判断用户是否登录
         String userId = (String) request.getAttribute("userId");
         if(userId!=null){
             // 先看未登录购物车中是否有数据
             List<CartInfo> cartListCookie = cartCookieHandler.getCartList(request);
             if(cartListCookie!=null&&cartListCookie.size()>0){
                 //ToDo有合并购物车
                 cartInfoList=  cartService.mergeToCartList(cartListCookie,userId);
                 // 删除未登录数据
                 cartCookieHandler.delCartList(request,response);
             }else{
                 //没有 直接去数库中获取
                 cartInfoList=  cartService.getCartList(userId);
             }
         }else{
             //直接从cookie中获取购物车
             cartInfoList  = cartCookieHandler.getCartList(request);

         }
           request.setAttribute("cartInfoList",cartInfoList);

          return "cartList";

    }
}
