package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单的方法
     */
    @Scheduled(cron = "0 * * * * ? ")   // 每分钟触发一次
    public void processTimeoutOrders() {
        log.info("处理超时订单");
        // 获取超时订单 当前时间 - 15
        List<Orders> orderList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));
        if (orderList != null && !orderList.isEmpty()) {
            orderList.forEach(order -> {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时，自动取消");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            });
        }
    }

    /**
     * 处理一直处于派送中的
     */
    @Scheduled(cron = "0 0 1 * * ? ")   // 每天凌晨一点触发
    public void processDeliveryOrders() {
        log.info("处理派送中的订单");
        // 获取派送中的订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> orderList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        if (orderList != null && !orderList.isEmpty()) {
            orderList.forEach(order -> {
                order.setStatus(Orders.COMPLETED);
                order.setDeliveryTime(LocalDateTime.now());
                orderMapper.update(order);
            });
        }
    }
}
