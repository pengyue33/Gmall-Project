package com.atgg.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atgg.gmall.been.CartInfo;
import com.atgg.gmall.been.OrderDetail;
import com.atgg.gmall.been.OrderInfo;
import com.atgg.gmall.been.UserAddress;
import com.atgg.gmall.been.enums.OrderStatus;
import com.atgg.gmall.been.enums.ProcessStatus;
import com.atgg.gmall.config.LoginRequire;
import com.atgg.gmall.service.CartService;
import com.atgg.gmall.service.OrderService;
import com.atgg.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class OrderController {
      @Reference
    CartService cartService;
     @Reference
    UserInfoService userInfoService;
     @Reference
    OrderService orderService;

     @RequestMapping("trade")
     @LoginRequire
    public String trade(HttpServletRequest request){
          //获取收货人信息
         String userId = (String) request.getAttribute("userId");
         List<UserAddress> userAddressList = userInfoService.getUserAddressList(userId);
         //获取选中的购物车列表
        List<CartInfo> cartCheckedList  =cartService.getCartCheckedList(userId);
        //赋值给订单详情列表
         List<OrderDetail> orderDetailList = new ArrayList<>();
         for (CartInfo cartInfo : cartCheckedList) {
             OrderDetail orderDetail = new OrderDetail();
             orderDetail.setSkuId(cartInfo.getSkuId());
             orderDetail.setSkuName(cartInfo.getSkuName());
             orderDetail.setImgUrl(cartInfo.getImgUrl());
             orderDetail.setSkuNum(cartInfo.getSkuNum());
             orderDetail.setOrderPrice(cartInfo.getCartPrice());
             orderDetailList.add(orderDetail);
         }
           //总价格信息
          OrderInfo orderInfo = new OrderInfo();
          orderInfo.setOrderDetailList(orderDetailList);
          orderInfo.sumTotalAmount();
          BigDecimal totalAmount = orderInfo.getTotalAmount();
          request.setAttribute("totalAmount",totalAmount);
          request.setAttribute("orderDetailList",orderDetailList);
          request.setAttribute("userAddressList",userAddressList);
          //保存流水号,防止表单重复提交
          String tradeNo= orderService.getTradeNo(userId);
          request.setAttribute("tradeNo",tradeNo);
         return "trade";
    }

    /**
     * 提交订单,将数据保存到数据库
     * @param orderInfo
     * @param request
     * @return
     */
    @RequestMapping("submitOrder")
    @LoginRequire
    public String  submitOrder (OrderInfo orderInfo,HttpServletRequest request){
         String  userId = (String) request.getAttribute("userId");
          //获取流水号，进行验证
        String tradeNo = request.getParameter("tradeNo");
        Boolean flag = orderService.checkTradeCode(userId, tradeNo);
         if(!flag){
               //验证失败，转发到错误页面
             request.setAttribute("errMsg","该页面已失效，请重新结算!");
              return "tradeFail";
         }
         //验证库存
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            Integer skuNum = orderDetail.getSkuNum();
            String skuId = orderDetail.getSkuId();
           boolean result = orderService.checkStock(skuId,skuNum);
            if(!result){
                //验证失败，转发到错误页面
                request.setAttribute("errMsg",""+orderDetail.getSkuName()+"库存不足!");
                return "tradeFail";
            }

        }
        // 初始化参数
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.setUserId(userId);
        orderInfo.sumTotalAmount();
        //保存 ,返回订单号
        String orderId = orderService.saveOrder(orderInfo);
        //认证成功，删除redis中流水号
        orderService.delTradeCode(userId);
        //重定向到 支付页面
         return  "redirect://payment.gmall.com/index?orderId="+orderId;
    }
     //拆单
    //http://order.gmall.com/orderSplit
    @RequestMapping("orderSplit")
    @ResponseBody
  public String orderSplit(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        String wareSkuMap = request.getParameter("wareSkuMap");
            //拆单完成返回子订单集合
      List<OrderInfo> orderInfoList= orderService.orderSplit(orderId,wareSkuMap);
      ArrayList<Map>  wareMapList = new ArrayList<>();
        for (OrderInfo orderInfo : orderInfoList) {
           Map map =  orderService.initWareOrder(orderInfo);
            wareMapList.add(map);
        }

         return JSON.toJSONString(wareMapList);
  }


}
