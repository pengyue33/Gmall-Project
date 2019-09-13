package com.atgg.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atgg.gmall.been.OrderDetail;
import com.atgg.gmall.been.OrderInfo;
import com.atgg.gmall.been.enums.ProcessStatus;
import com.atgg.gmall.order.mapper.OrderDetailMapper;
import com.atgg.gmall.order.mapper.OrderInfoMapper;
import com.atgg.gmall.service.OrderService;
import com.atgg.gmall.service.PaymentService;
import com.atgg.gmall.service.util.ActiveMQUtil;
import com.atgg.gmall.service.util.RedisUtil;
import com.atgg.gmall.util.HttpClientUtil;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

@Service
public class OrderServceImpl implements OrderService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ActiveMQUtil activeMQUtil;
    @Reference
    private PaymentService paymentService;

    /**
     * 保存订单
     *
     * @param orderInfo
     * @return
     */
    //添加事务
    @Transactional
    @Override
    public String saveOrder(OrderInfo orderInfo) {
        //设置创建时间
        orderInfo.setCreateTime(new Date());
        //设置失效时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        orderInfo.setExpireTime(calendar.getTime());
        //生成第三方支付编号
        String outTradeNo = "ATGUIGU" + System.currentTimeMillis() + "" + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        //插入订单信息
        orderInfoMapper.insertSelective(orderInfo);
        //插入订单详情信息
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            //设置订单id
            orderDetail.setOrderId(orderInfo.getId());
            //置空主键
            orderDetail.setId(null);
            orderDetailMapper.insertSelective(orderDetail);
        }
        //返回订单id
        return orderInfo.getId();
    }

    /**
     * 生成流水号，防止表单重复提交
     *
     * @param userId
     * @return
     */
    @Override
    public String getTradeNo(String userId) {
        //生成随机字符串作为流水号
        String tradeNovalue = UUID.randomUUID().toString().replace("-", "");
        //存储到redis中，作为比较
        Jedis jedis = redisUtil.getJedis();
        //key
        String tradeNoKey="user:"+userId+":tradeCode";
        jedis.set(tradeNoKey,tradeNovalue);
        jedis.close();
        return tradeNovalue;
    }

    /**
     * 验证流水号
     * @param userId
     * @param tradeNo
     * @return
     */
    @Override
    public Boolean checkTradeCode(String userId, String tradeNo) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:"+userId+":tradeCode";
        String tradeCode = jedis.get(tradeNoKey);
        jedis.close();

        return tradeNo.equals(tradeCode) ;
    }

    /**
     * 删除redis中流水号
     * @param userId
     */
    @Override
    public void delTradeCode(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:"+userId+":tradeCode";
        jedis.del(tradeNoKey);
        jedis.close();
    }

    /**
     * 验证库存
     * @param skuId
     * @param skuNum
     * @return
     */
    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        if ("1".equals(result)){
            return  true;
        }else {
            return  false;
        }
    }

      @Override
    public OrderInfo getOrderInfoById(String orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
          //查询订单详情
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
        orderInfo.setOrderDetailList(orderDetailList);
        return orderInfo;
    }

    /**
     * 修改订单状态
     * @param orderId
     * @param paid
     */
    @Override
    public void updateOrderStatus(String orderId, ProcessStatus paid) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(paid);
        orderInfo.setOrderStatus(paid.getOrderStatus());
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);

    }

    /**
     * 通知减库存消息队列
     * @param orderId
     */
     @Override
    public void sendOrderStatus(String orderId) {
         Connection connection = activeMQUtil.getConnection();
         try {
             Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
             Queue order_result_queue = session.createQueue("ORDER_RESULT_QUEUE");
             MessageProducer producer = session.createProducer(order_result_queue);
             ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
             String orderJson = initWareOrder(orderId);
             activeMQTextMessage.setText(orderJson);
             producer.send(activeMQTextMessage);
              //提交
             session.commit();
             session.close();
             producer.close();
             connection.close();

         } catch (JMSException e) {
             e.printStackTrace();
         }
     }

    /**
     * 获取过期订单
     * @return
     */
    @Override
    public List<OrderInfo> getExpiredOrderList() {
        Example example = new Example(OrderInfo.class);
        example.createCriteria().andLessThan("expireTime",
                new Date()).andEqualTo("processStatus",ProcessStatus.UNPAID);
        List<OrderInfo> orderInfoList = orderInfoMapper.selectByExample(example);
        return orderInfoList;
    }

    /**
     * 修改过期订单的状态
     * @param orderInfo
     */
    // 处理未完成订单
     @Async
     @Override
    public void execExpiredOrder(OrderInfo orderInfo) {
         // 订单信息
         updateOrderStatus(orderInfo.getId(),ProcessStatus.CLOSED);
         //关闭付款信息
         paymentService.closePayment(orderInfo.getId());
     }

    private String initWareOrder(String orderId) {
        OrderInfo orderInfo = getOrderInfoById(orderId);
         Map map =   initWareOrder(orderInfo);
         return JSON.toJSONString(map);
    }
       @Override
     public Map initWareOrder(OrderInfo orderInfo) {
        HashMap<String, Object> map  = new HashMap<>();
        map.put("orderId",orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());
        map.put("orderBody","订单概要");
        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        map.put("paymentWay","2");
        map.put("wareId",orderInfo.getWareId());
       List detailList = new ArrayList();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            Map detailMap = new HashMap();
            detailMap.put("skuId",orderDetail.getSkuId());
            detailMap.put("skuName",orderDetail.getSkuName());
            detailMap.put("skuNum",orderDetail.getSkuNum());
            detailList.add(detailMap);
        }
        map.put("details",detailList);
         return  map;
    }

    @Override
    public List<OrderInfo> orderSplit(String orderId, String wareSkuMap) {
        List<OrderInfo> subOrderInfoList = new ArrayList<>();
//        1.  获取原始订单

//        2.  需要将wareSkuMap[{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}] 中的数据判断是否需要拆单并写拆单规则
//        wareSkuMap 转换为我们能操作的对象

//        3.  创建新的子订单
//        4.  给新的子订单赋值
//        5.  保存子订单
//        6.  将子订单添加到集合中List<OrderInfo>
//        7.  更新原始订单的状态！

        OrderInfo orderInfo = getOrderInfoById(orderId);
        List<Map> maps = JSON.parseArray(wareSkuMap, Map.class);
        if(maps!=null && maps.size()>0){
            for (Map map : maps) {
               // {"wareId":"1","skuIds":["2","10"]}
                String  wareId = (String) map.get("wareId");
                List<String> skuIds = (List<String>) map.get("skuIds");
                //创建新的子订单
                OrderInfo subOrderInfo = new OrderInfo();
                // 属性赋值
                BeanUtils.copyProperties(orderInfo,subOrderInfo);
                //防止主键冲突
                subOrderInfo.setId(null);
                // 声明一个集合来存储子订单明细
                ArrayList<OrderDetail> orderDetailsList = new ArrayList<>();
                // 订单明细
                List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
                if(orderDetailList!=null && orderDetailList.size()>0) {
                    for (OrderDetail orderDetail : orderDetailList) {
                        for (String skuId : skuIds) {
                            if (orderDetail.getSkuId().equals(skuId)) {
                                orderDetailsList.add(orderDetail);
                            }
                        }
                    }
                }
                //
                subOrderInfo.setOrderDetailList(orderDetailsList);
                // 赋值仓库Id
                subOrderInfo.sumTotalAmount();
                // 赋值父订单Id
                subOrderInfo.setParentOrderId(orderId);
                // 保存子订单
                saveOrder(subOrderInfo);
                // 添加子订单
                subOrderInfoList.add(subOrderInfo);
            }
        }
             // 修改原始订单的状态！
        updateOrderStatus(orderId,ProcessStatus.SPLIT);
        // 8 返回一个新生成的子订单列表
         return subOrderInfoList;
    }

}
