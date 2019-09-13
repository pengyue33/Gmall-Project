package com.atgg.gmall.service;

import com.atgg.gmall.been.OrderInfo;
import com.atgg.gmall.been.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

public interface OrderService {
     //保存订单
    String saveOrder(OrderInfo orderInfo);
    //生成流水号
    String getTradeNo(String userId);
    //验证流水号
    Boolean checkTradeCode(String userId, String tradeNo);
    //删除流水号
    void delTradeCode(String userId);
    //验证库存
    public boolean checkStock(String skuId, Integer skuNum);
    //根据Id 获取订单信息
   public   OrderInfo getOrderInfoById(String orderId);
   //修改订单状态
    void updateOrderStatus(String orderId, ProcessStatus paid);
      //通知减少库存
    void sendOrderStatus(String orderId);
     //获取过期订单
    List<OrderInfo> getExpiredOrderList();
     //修改过期订单状态
    void execExpiredOrder(OrderInfo orderInfo);

    public Map initWareOrder(OrderInfo orderInfo);
       //拆单
     List<OrderInfo> orderSplit(String orderId, String wareSkuMap);
}
