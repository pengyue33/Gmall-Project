package com.atgg.gmall.service;

import com.atgg.gmall.been.PaymentInfo;

import java.util.Map;

public interface PaymentService {
        //保存支付信息
    void savePaymentInfo(PaymentInfo paymentInfo);

    PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery);
         //修改支付订单状态
    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUPD);

    public boolean refund(String orderId);
      //生成微信二维码
     Map createNative(String orderId, String totalAmout);
       //发送消息队列给 订单模块
    void sendPaymentResult(PaymentInfo paymentInfo, String result);
       //询问支付宝支付状态
    boolean checkPayment(PaymentInfo paymentInfoQuery);
       //发送延迟队列，询问支付状态
    public void sendDelayPaymentResult(String outTradeNo,int delaySec ,int checkCount);
       //关闭交易
    void closePayment(String id);
}
