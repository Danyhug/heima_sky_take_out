package com.sky.controller.user;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@ApiOperation("订单接口")
@RequestMapping("/user/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO dto) {
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(dto);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 订单支付
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }

    @GetMapping("/historyOrders")
    @ApiOperation("分页查询历史订单")
    public Result<PageResult<OrderVO>> historyOrders(int page, int pageSize, Integer status) {
        PageResult<OrderVO> pageResult = orderService.pageQueryByUser(page, pageSize, status);
        return Result.success(pageResult);
    }

    /**
     * 再来一单
     *
     * @param id
     * @return
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result<String> repetition(@PathVariable Long id) {
        orderService.repetition(id);
        return Result.success();
    }

    /**
     * 催单
     */
    @GetMapping("/reminder/{id}")
    @ApiOperation("催单")
    public Result reminder(@PathVariable Long id) {
        orderService.reminder(id);
        return Result.success();
    }
}
