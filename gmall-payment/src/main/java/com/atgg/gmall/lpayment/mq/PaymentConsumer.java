package com.atgg.gmall.lpayment.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atgg.gmall.been.PaymentInfo;
import com.atgg.gmall.service.PaymentService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class PaymentConsumer {
     @Reference
    private PaymentService paymentService;

      @JmsListener(destination ="PAYMENT_RESULT_CHECK_QUEUE" ,containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(MapMessage mapMessage) throws JMSException {
         //获去消息队列中的数据
         String outTradeNo = mapMessage.getString("outTradeNo");
         int delaySec = mapMessage.getInt("delaySec");
         int checkCount = mapMessage.getInt("checkCount");
         PaymentInfo paymentInfo = new PaymentInfo();
         paymentInfo.setOutTradeNo(outTradeNo);
         //调用检查订单状态服务
         boolean b = paymentService.checkPayment(paymentInfo);
         System.out.println("检查结果："+b);
         if(!b&&checkCount>0){
                //继续检查
             System.out.println("检查的次数："+checkCount);
             paymentService.sendDelayPaymentResult(outTradeNo,delaySec,checkCount-1);
         }

     }


}
