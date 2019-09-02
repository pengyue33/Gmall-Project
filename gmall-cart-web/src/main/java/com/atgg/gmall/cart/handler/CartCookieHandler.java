package com.atgg.gmall.cart.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atgg.gmall.been.CartInfo;
import com.atgg.gmall.been.SkuInfo;
import com.atgg.gmall.config.CookieUtil;
import com.atgg.gmall.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class CartCookieHandler {

    @Reference
    private ManageService manageService;
    // 定义购物车名称
    private String cookieCartName = "CART";
    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE=7*24*3600;
    /**
     * 未登录状态下，将购物项保存到cookie中
     * @param request
     * @param response
     * @param skuId
     * @param skuNum
     */
    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, int skuNum) {
         //获取cookie中原有购物车，及其中的购物项，存在改变数量
        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);
        List<CartInfo> cartInfos =new ArrayList<>();
         // 定义一个 boolean 类型的变量
         boolean ifExist=false;
        if(!StringUtils.isEmpty(cookieValue)){
               //获取购物车中的所有购物项
            cartInfos = JSON.parseArray(cookieValue, CartInfo.class);
             for (CartInfo cartInfo : cartInfos) {
                     if(skuId.equals(cartInfo.getSkuId())){
                         //新加的购物在原购物车中存在,改变数量即可
                         cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
                          //更新价格
                         cartInfo.setSkuPrice(cartInfo.getCartPrice());
                         //改变标志旗
                         ifExist=true;
                     }
                }
        }
         if(!ifExist) {
             //新加的购物在原购物车中不存在，从数据库中查询，并赋值，最后更新cookie中购物车
             SkuInfo skuInfo = manageService.getSkuInfoBySkuId(skuId);
             CartInfo cartInfo = new CartInfo();
             cartInfo.setSkuId(skuId);
             cartInfo.setCartPrice(skuInfo.getPrice());
             cartInfo.setSkuPrice(skuInfo.getPrice());
             cartInfo.setSkuName(skuInfo.getSkuName());
             cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
             cartInfo.setSkuNum(skuNum);
              //填入购物车集合中
             cartInfos.add(cartInfo);
         }
           //更新购物车
        CookieUtil.setCookie(request,response,cookieCartName,JSON.toJSONString(cartInfos),COOKIE_CART_MAXAGE,true);

     }

    /**
     * 获取cookie中购物车中列表信息
     * @param request
     * @return
     */
      public List<CartInfo> getCartList(HttpServletRequest request) {
           List<CartInfo> cartInfos=new ArrayList<>();
          //获取cookie中原有购物车，及其中的购物项，存在改变数量
          String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);
           if(!StringUtils.isEmpty(cookieValue)){
                 cartInfos = JSON.parseArray(cookieValue, CartInfo.class);
           }
            return cartInfos;
    }

    /**
     * 合并完成后，删除Cookie中购物项
     * @param request
     * @param response
     */

    public void delCartList(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request,response,cookieCartName);
    }

    /**
     * 修改购物车中购物项的选中状态
     * @param request
     * @param response
     * @param isChecked
     * @param skuId
     */
    public void checkCart(HttpServletRequest request, HttpServletResponse response, String isChecked, String skuId) {
               //获取所有的购物项
            List<CartInfo> cartList = getCartList(request);
            for (CartInfo cartInfo : cartList) {
                if(cartInfo.getSkuId().equals(skuId)){
                    cartInfo.setIsChecked(isChecked);
                }
        }
            //更新到cookie
           CookieUtil.setCookie(request,response,cookieCartName,JSON.toJSONString(cartList),COOKIE_CART_MAXAGE,true);
    }
}
