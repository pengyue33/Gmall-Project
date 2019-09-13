package com.atgg.gmall.order.task;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atgg.gmall.been.OrderInfo;
import com.atgg.gmall.service.OrderService;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
public class OrderTask {
       @Reference
   private  OrderService orderService;

//    @Scheduled(cron = "5 * * * * ?")
//    public void  work(){
//        System.out.println("Thread ====== "+ Thread.currentThread());
//    }
//    @Scheduled(cron = "0/5 * * * * ?")
//    public void  work1(){
//        System.out.println("Thread1 ====== "+ Thread.currentThread());
//    }
      //处理过期订单
      @Scheduled(cron = "0/20 * * * * ?")
    public void checkOrder(){
        System.out.println("开始处理过期订单");
      List<OrderInfo> orderInfoList= orderService.getExpiredOrderList();
         for (OrderInfo orderInfo : orderInfoList) {
             orderService.execExpiredOrder(orderInfo);
         }
          System.out.println("一共处理"+orderInfoList.size()+"个订单" );
    }


}
