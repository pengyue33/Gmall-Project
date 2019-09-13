package com.atgg.gmall.lpayment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atgg.gmall.been.OrderInfo;
import com.atgg.gmall.been.PaymentInfo;
import com.atgg.gmall.been.enums.PaymentStatus;
import com.atgg.gmall.config.LoginRequire;
import com.atgg.gmall.lpayment.config.AlipayConfig;
import com.atgg.gmall.service.OrderService;
import com.atgg.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.apache.catalina.manager.host.Constants.CHARSET;

@Controller
public class PaymentController {
          @Autowired
       AlipayClient alipayClient;
         @Reference
       OrderService orderService;
        @Reference
      PaymentService paymentService;

    @RequestMapping("index")
    @LoginRequire
    public String toIndex(HttpServletRequest request) {
        String orderId = request.getParameter("orderId");
        //获取订单信息
        OrderInfo orderInfo = orderService.getOrderInfoById(orderId);
        request.setAttribute("orderId", orderId);
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());
        return "index";
    }

    @RequestMapping("alipay/submit")
    @ResponseBody
    public String submitPayment(HttpServletRequest request, HttpServletResponse response) {
        //将订单信息保存到支付信息的数据库中
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfoById(orderId);
        //保存支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject("买一部手机玩玩！");
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setCreateTime(new Date());

        paymentService.savePaymentInfo(paymentInfo);

        //调用支付宝接口，生成二维码页面
//          AlipayClient alipayClient = new DefaultAlipayClient(
//                   "https://openapi.alipay.com/gateway.do",
//                    APP_ID,
//                    APP_PRIVATE_KEY,
//                    FORMAT,
//                    CHARSET,
//                    ALIPAY_PUBLIC_KEY,
//                   // 商户生成签名字符串所使用的签名算法类型，目前支持 RSA2 和 RSA，推荐使用 RSA2
//                  SIGN_TYPE); //获得初始化的AlipayClient
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url); //同步回调
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址

        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("subject",paymentInfo.getSubject());
        map.put("total_amount",paymentInfo.getTotalAmount());
        String bizContent = JSON.toJSONString(map);

        alipayRequest.setBizContent(bizContent);

       // alipayRequest.setBizContent("{" +
//                "    \"out_trade_no\":\"20150320010101001\"," +
//                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
//                "    \"total_amount\":88.88," +
//                "    \"subject\":\"Iphone6 16G\"," +
//                "    \"body\":\"Iphone6 16G\"," +
//                "    \"passback_params\":\"merchantBizType%3d3C%26merchantBizNo%3d2016010101111\"," +
//                "    \"extend_params\":{" +
//                "    \"sys_service_provider_id\":\"2088511833207846\"" +
//                "    }" +
//                "  }");//填充业务参数

        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
         response.setContentType("text/html;charset=" + AlipayConfig.charset);

         //查询支付状态
        // 15秒执行一次，总共需要执行3次。
        paymentService.sendDelayPaymentResult(paymentInfo.getOutTradeNo(),15,3);

        return form;
    }
    //
    // 同步回调：
    @RequestMapping("alipay/callback/return")
    public String callbackReturn(){
        // 返回订单页面！
        // 清空购物车 。。。！jeids.del(key);
        return "redirect:"+AlipayConfig.return_order_url;
    }

    // 异步回调：通知商家是否支付成功！
    //1、	验证回调信息的真伪
    //2、	验证用户付款的成功与否
    //3、	把新的支付状态写入支付信息表{paymentInfo}中。
    //4、	通知电商
    //5、	给支付宝返回回执。

     @RequestMapping("alipay/callback/notify")
     public String callbackNotify(@RequestParam Map<String,String> paramMap, HttpServletRequest request){

         // Map<String, String> paramsMap = ... //将异步通知中收到的所有参数都存放到map中
         try {
             boolean flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, CHARSET, AlipayConfig.sign_type); //调用SDK验证签名
             // 交易状态
             String trade_status = paramMap.get("trade_status");
             // 获取交易编号
             String out_trade_no = paramMap.get("out_trade_no");
             if(flag){
                 // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure

                 if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){
                     // 进一步判断：记录交易日志表的交易状态
                     // select * from paymentInfo where out_trade_no = ?
                     // 调用服务层
                     PaymentInfo paymentInfoQuery = new PaymentInfo();
                     paymentInfoQuery.setOutTradeNo(out_trade_no);
                     PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentInfoQuery);

                     if (paymentInfo.getPaymentStatus()==PaymentStatus.PAID || paymentInfo.getPaymentStatus()==PaymentStatus.ClOSED){
                         return "failure";
                     }
                     // 支付成功应该修改交易记录状态
                     // update paymentInfo set getPaymentStatus = aymentStatus.PAID where out_trade_no=out_trade_no
                     PaymentInfo paymentInfoUPD = new PaymentInfo();
                     paymentInfoUPD.setPaymentStatus(PaymentStatus.PAID);
                     paymentInfoUPD.setCreateTime(new Date());
                     // 更新交易记录
                     paymentService.updatePaymentInfo(out_trade_no,paymentInfoUPD);
                     // 支付成功！订单状态变成支付！ 发送消息队列！


                     return "success";
                 }
             }else{
                 // TODO 验签失败则记录异常日志，并在response中返回failure.
                 return "failure";
             }
         } catch (AlipayApiException e) {
             e.printStackTrace();
         }
         return "failure";
     }
    //支付宝退款
    // payment.gmall.com/refund?orderId=100
    @RequestMapping("refund")
    @ResponseBody
    public String refund(String orderId){
        // 调用服务层接口
        boolean result = paymentService.refund(orderId);
        return ""+result;
    }
   //微信支付，二维码生成
       @RequestMapping("wx/submit")
       @ResponseBody
    public Map createNative(HttpServletRequest request){
           String orderId = request.getParameter("orderId");
          // Object orderId1 = request.getAttribute("orderId");
           //第一个参数是订单Id ，第二个参数是多少钱，单位是分
        Map map = paymentService.createNative(orderId +"", "1");
        System.out.println(map.get("code_url"));
         // data = map
         return map;
    }

      //异步返回支付结果，更改支付信息后，发送消息队列通知 订单系统 更改订单状态
     //模拟方法
     // payment.gmall.com/sendPaymentResult?orderId=115result=success
    @RequestMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo,String result){
        paymentService.sendPaymentResult(paymentInfo,result);
        return "to success";
    }
    //查询订单信息，询问支付宝支付状态
    // payment.gmall.com/queryPaymentResult?orderId=xxx
    @RequestMapping("queryPaymentResult")
    @ResponseBody
    public String queryPaymentResult(PaymentInfo paymentInfo){
        boolean flag = paymentService.checkPayment(paymentInfo);
        return "flag="+flag;
    }


}
