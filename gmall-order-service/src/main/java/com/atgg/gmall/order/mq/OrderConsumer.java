package com.atgg.gmall.order.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atgg.gmall.been.OrderInfo;
import com.atgg.gmall.been.enums.ProcessStatus;
import com.atgg.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderConsumer {
            @Reference
    OrderService orderService;
       @JmsListener(destination = "PAYMENT_RESULT_QUEUE",containerFactory = "jmsQueueListener")
     public void consumerPaymentResult(MapMessage mapMessage) throws JMSException {
           String orderId = mapMessage.getString("orderId");
           String result = mapMessage.getString("result");
           if("success".equals(result)){
               //支付成功，修改订单状态
               orderService.updateOrderStatus(orderId,ProcessStatus.PAID);
               //发送消息队列，库存模块。减库存
               orderService.sendOrderStatus(orderId);

           }else{
               orderService.updateOrderStatus(orderId,ProcessStatus.UNPAID);
           }
       }
       //获取库存系统发送的减库存完成消息
    @JmsListener(destination = "SKU_DEDUCT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(MapMessage mapMessage) throws JMSException {
        String orderId = mapMessage.getString("orderId");
        String status = mapMessage.getString("status");
        String trackingNo = mapMessage.getString("trackingNo");
        if("DEDUCTED".equals(status)){
            orderService.updateOrderStatus(  orderId , ProcessStatus.WAITING_DELEVER);
        }else{
            orderService.updateOrderStatus(  orderId , ProcessStatus.STOCK_EXCEPTION);
        }


    }
}
