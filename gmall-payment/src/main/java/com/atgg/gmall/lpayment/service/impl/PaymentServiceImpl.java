package com.atgg.gmall.lpayment.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.WebUtils;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.atgg.gmall.been.OrderInfo;
import com.atgg.gmall.been.PaymentInfo;
import com.atgg.gmall.been.enums.PaymentStatus;
import com.atgg.gmall.lpayment.mapper.PaymentMapper;
import com.atgg.gmall.service.OrderService;
import com.atgg.gmall.service.PaymentService;
import com.atgg.gmall.service.util.ActiveMQUtil;
import com.atgg.gmall.util.HttpClient;
import com.github.wxpay.sdk.WXPayUtil;
import org.apache.activemq.ActiveMQMessageProducer;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentMapper paymentMapper;
    @Reference
    private OrderService orderService;

    @Autowired
    private AlipayClient alipayClient;
    // 服务号Id
    @Value("${appid}")
    private String appid;
    // 商户号Id
    @Value("${partner}")
    private String partner;
    // 密钥
    @Value("${partnerkey}")
    private String partnerkey;
    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentMapper.insertSelective(paymentInfo);
    }

    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery) {
        return paymentMapper.selectOne(paymentInfoQuery);
    }

    @Override
    public void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUPD) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo", out_trade_no);

        paymentMapper.updateByExampleSelective(paymentInfoUPD, example);
    }

    @Override
    public boolean refund(String orderId) {
        // AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        // 根据orderId 查询OrderInfo
        OrderInfo orderInfo = orderService.getOrderInfoById(orderId);
        // 设置map 封装参数
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no", orderInfo.getOutTradeNo());
        map.put("refund_amount", orderInfo.getTotalAmount());
        map.put("refund_reason", "oooo");
        map.put("out_request_no", "HZ01RF001");
        request.setBizContent(JSON.toJSONString(map));

//        request.setBizContent("{" +
//                "\"out_trade_no\":\"20150320010101001\"," +
//                "\"trade_no\":\"2014112611001004680073956707\"," +
//                "\"refund_amount\":200.12," +
//                "\"refund_currency\":\"USD\"," +
//                "\"refund_reason\":\"正常退款\"," +
//                "\"out_request_no\":\"HZ01RF001\"," +
//                "\"operator_id\":\"OP001\"," +
//                "\"store_id\":\"NJ_S_001\"," +
//                "\"terminal_id\":\"NJ_T_001\"," +
//                "      \"goods_detail\":[{" +
//                "        \"goods_id\":\"apple-01\"," +
//                "\"alipay_goods_id\":\"20010001\"," +
//                "\"goods_name\":\"ipad\"," +
//                "\"quantity\":1," +
//                "\"price\":2000," +
//                "\"goods_category\":\"34543238\"," +
//                "\"categories_tree\":\"124868003|126232002|126252004\"," +
//                "\"body\":\"特价手机\"," +
//                "\"show_url\":\"http://www.alipay.com/xxx.jpg\"" +
//                "        }]," +
//                "      \"refund_royalty_parameters\":[{" +
//                "        \"royalty_type\":\"transfer\"," +
//                "\"trans_out\":\"2088101126765726\"," +
//                "\"trans_out_type\":\"userId\"," +
//                "\"trans_in_type\":\"userId\"," +
//                "\"trans_in\":\"2088101126708402\"," +
//                "\"amount\":0.1," +
//                "\"amount_percentage\":100," +
//                "\"desc\":\"分账给2088101126708402\"" +
//                "        }]," +
//                "\"org_pid\":\"2088101117952222\"" +
//                "  }");
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        if (response.isSuccess()) {

            System.out.println("调用成功");
            return true;
        } else {
            System.out.println("调用失败");
            return false;
        }

    }

    /**
     * 生成微信支付二维码
     * @param orderId
     * @param totalAmout
     * @return
     */
     @Override
    public Map createNative(String orderId, String totalAmout) {
         //1.创建参数
         Map<String,String> param=new HashMap();//创建参数
         param.put("appid", appid);//公众号
         param.put("mch_id", partner);//商户号
         param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
         param.put("body", "尚硅谷");//商品描述
         param.put("out_trade_no", orderId);//商户订单号
         param.put("total_fee",totalAmout);//总金额（分）
         param.put("spbill_create_ip", "127.0.0.1");//IP
         param.put("notify_url", "http://order.gmall.com/trade");//回调地址(随便写)
         param.put("trade_type", "NATIVE");//交易类型
         try {
             //必须以xml方式发送
             String xml = WXPayUtil.generateSignedXml(param, partnerkey);
             HttpClient client=new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
             client.setHttps(true); //https
             client.setXmlParam(xml);
             client.post();  //发送
             //获取返回结果
             String result = client.getContent();
             Map<String, String> stringStringMap = WXPayUtil.xmlToMap(result);
             Map<String, String> map=new HashMap<>();
             map.put("code_url", stringStringMap.get("code_url"));//支付地址
             map.put("total_fee", totalAmout);//总金额
             map.put("out_trade_no",orderId);//订单号

             return map;
         } catch (Exception e) {
             e.printStackTrace();
         }

                return null;
    }

    /**
     * 发送消息队列给订单模块
     * @param paymentInfo
     * @param result
     */
    @Override
    public void sendPaymentResult(PaymentInfo paymentInfo, String result) {
           //获取连接
        Connection connection = activeMQUtil.getConnection();
        try {
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
              //创建队列
            Queue payment_result_queue = session.createQueue("PAYMENT_RESULT_QUEUE");
             //创建消息提供者
            MessageProducer producer = session.createProducer(payment_result_queue);
            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
            activeMQMapMessage.setString("orderId",paymentInfo.getOrderId());
            activeMQMapMessage.setString("result",result);
            //发送消息
            producer.send(activeMQMapMessage);
            //提交
            session.commit();
            //关闭
            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询订单信息，询问支付宝支付状态
     * @param paymentInfoQuery
     * @return
     */
    @Override
    public boolean checkPayment(PaymentInfo paymentInfoQuery) {
           //查询当前订单支付状态
        PaymentInfo paymentInfo = getPaymentInfo(paymentInfoQuery);

        if (paymentInfo.getPaymentStatus()== PaymentStatus.PAID || paymentInfo.getPaymentStatus()==PaymentStatus.ClOSED){
               return true;
        }
          //访问支付宝支付查询状态
          //AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        HashMap<String, Object> paramMap= new HashMap<>();
           paramMap.put("out_trade_no",paymentInfo.getOutTradeNo());
//        request.setBizContent("{" +
//                "\"out_trade_no\":\"20150320010101001\"," +
//                "\"trade_no\":\"2014112611001004680 073956707\"," +
//                "\"org_pid\":\"2088101117952222\"," +
//                "      \"query_options\":[" +
//                "        \"TRADE_SETTE_INFO\"" +
//                "      ]" +
//                "  }");
            //传递参数
        request.setBizContent(JSON.toJSONString(paramMap));
        AlipayTradeQueryResponse response = null;
        try {
              response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("调用成功");
            if("TRADE_SUCCESS".equals(response.getTradeStatus())||"TRADE_FINISHED".equals(response.getTradeStatus())){
                 System.out.println("支付成功");
                // 更新交易记录的状态 paymentInfo
                // 改支付状态
                PaymentInfo payment = new PaymentInfo();
                payment.setPaymentStatus(PaymentStatus.PAID);
                updatePaymentInfo(paymentInfo.getOutTradeNo(),payment);
                      //发送消息 给订单模块
                 sendPaymentResult(paymentInfo,"success");
            }
        } else {
             System.out.println("调用失败");
        }


        return false;
    }
     //发送延迟队列，询问支付状态
    /**
     * 延迟队列反复调用
     * @param outTradeNo 单号
     * @param delaySec 延迟秒
     * @param checkCount 几次
     */

    @Override
    public void sendDelayPaymentResult(String outTradeNo, int delaySec, int checkCount) {
        Connection connection = activeMQUtil.getConnection();
        try {
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue payment_result_check_queue = session.createQueue("PAYMENT_RESULT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(payment_result_check_queue);
            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
            activeMQMapMessage.setString("outTradeNo",outTradeNo);
            activeMQMapMessage.setInt("delaySec",delaySec);
            activeMQMapMessage.setInt("checkCount",checkCount);
            // 设置延迟多少时间
            activeMQMapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,delaySec*1000);
            producer.send(activeMQMapMessage);
               //提交
            session.commit();
              //关闭
            producer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭交易
     * @param orderId
     */
    @Override
    public void closePayment(String orderId) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderId",orderId);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus(PaymentStatus.ClOSED);
        paymentMapper.updateByExampleSelective(paymentInfo,example);
    }
}
