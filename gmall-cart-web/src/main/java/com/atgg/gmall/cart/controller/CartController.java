package com.atgg.gmall.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atgg.gmall.been.CartInfo;
import com.atgg.gmall.been.SkuInfo;
import com.atgg.gmall.cart.handler.CartCookieHandler;
import com.atgg.gmall.config.CookieUtil;
import com.atgg.gmall.config.LoginRequire;
import com.atgg.gmall.service.CartService;
import com.atgg.gmall.service.ManageService;
import org.apache.http.HttpResponse;
import org.omg.CORBA.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.CookieHandler;
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

    /**
     * 购物项是否勾选
     * @param request
     */
      @RequestMapping("checkCart")
      @LoginRequire(autoRedirect = false)
      @ResponseBody
      public void checkCart(HttpServletRequest request,HttpServletResponse response){
          String userId = (String) request.getAttribute("userId");
          String isChecked = request.getParameter("isChecked");
          String skuId = request.getParameter("skuId");
          //判断用户是否登录
          if(userId!=null){
                    //登录，修改redis中购物车状态，并构建新的选中的购物车
               cartService.checkCart(isChecked,skuId,userId);
          }else{
                 //未登录,修改cookie购物车状态
              cartCookieHandler.checkCart(request,response,isChecked,skuId);
          }
      }

    /**
     * 去结算，用户必须登录，并合并购物车，将勾选中的购物车更新
     * @return
     */
           @RequestMapping("toTrade")
           @LoginRequire
        public String toTrade(HttpServletRequest request, HttpServletResponse response){
               String  userId = (String) request.getAttribute("userId");
               //从cookie中获取未登录的购物车
               List<CartInfo> cartCookieList = cartCookieHandler.getCartList(request);
                  if(cartCookieList!=null&& cartCookieList.size()>0){
                        //合并购物车
                      cartService.mergeToCartList(cartCookieList,userId);
                       // 删除未登录数据
                      cartCookieHandler.delCartList(request,response);
                  }
                return  "redirect://order.gmall.com/trade";
      }


}
