package com.atgg.gmall.been;

import com.atgg.gmall.been.enums.OrderStatus;
import com.atgg.gmall.been.enums.PaymentWay;
import com.atgg.gmall.been.enums.ProcessStatus;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderInfo implements Serializable {
    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column
    private String consignee;
    @Column
    private String consigneeTel;

    @Column
    private BigDecimal totalAmount;
    //订单状态，用于显示给用户查看。设定初始值。
    @Column
    private OrderStatus orderStatus;
   //  订单进度状态，程序控制、 后台管理查看。设定初始值，
    @Column
    private ProcessStatus processStatus;

    @Column
    private String userId;

    @Column
    private PaymentWay paymentWay;
     //过期时间
    @Column
    private Date expireTime;

    @Column
    private String deliveryAddress;

    @Column
    private String orderComment;

    @Column
    private Date createTime;

    @Column
    private String parentOrderId;

    @Column
    private String trackingNo;


    @Transient
    private List<OrderDetail> orderDetailList;


    @Transient
    private String wareId;

    @Column
    //第三方支付编号。按规则生成
    private String outTradeNo;

    public void sumTotalAmount(){
        BigDecimal totalAmount=new BigDecimal("0");
        for (OrderDetail orderDetail : orderDetailList) {
            totalAmount= totalAmount.add(orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum())));
        }
        this.totalAmount=  totalAmount;
    }

}
