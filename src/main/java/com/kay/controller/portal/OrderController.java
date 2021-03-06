package com.kay.controller.portal;

//import com.alipay.demo.trade.config.Configs;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.kay.service.AlipayConstants;
import com.kay.service.AuthService;
import com.kay.service.OrderService;
import com.kay.vo.OrderProductVo;
import com.kay.vo.OrderVo;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by kay on 2018/3/27.
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final AuthService authService;

    @Autowired
    public OrderController(OrderService orderService, AuthService authService) {
        this.orderService = orderService;
        this.authService = authService;
    }

    /**
     * 创建订单
     */
    @GetMapping("/create")
    public OrderVo create(HttpServletRequest request, Integer shippingId) {
        return orderService.createOrder(getUserId(request), shippingId);
    }

    /**
     * 取消订单
     */
    @GetMapping("/cancel")
    public void cancel(Long orderNo, HttpServletRequest request) {
        orderService.cancelOrder(getUserId(request), orderNo);
    }

    //获取购物车中已经选中的商品详情
    @GetMapping("/get_order_cart_product")
    public OrderProductVo getOrderCartProduct(HttpServletRequest request) {
        return orderService.getOrderCartProduct(getUserId(request));
    }

    @GetMapping("/detail")
    public OrderVo getOrderDetail(HttpServletRequest request, Long orderNo) {
        return orderService.getOrderDetail(getUserId(request), orderNo);
    }

    @GetMapping("/list")
    public PageInfo getOrderList(HttpServletRequest request,
                                 @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                 @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return orderService.getOrderList(getUserId(request), pageNum, pageSize);
    }

    /**
     * 支付
     */
    @GetMapping("/pay")
    public Map<String, String> pay(Long orderNo, HttpServletRequest request) {
        Integer userId = getUserId(request);
        String path = request.getSession().getServletContext().getRealPath("upload");
        return orderService.pay(userId, orderNo, path);
    }

    /**
     * 支付宝回调处理接口
     *
     * @param request
     * @return
     */
    @GetMapping("/alipay_callback")
    public String alipayCallback(HttpServletRequest request) {
        Map<String, String> params = Maps.newHashMap();

        Map requestParams = request.getParameterMap();
        for (Iterator iterator = requestParams.keySet().iterator(); iterator.hasNext(); ) {
            String name = (String) iterator.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                //拼接 valueStr,用逗号拼接，最后一次不加逗号
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }

            params.put(name, valueStr);
        }
        log.info("支付宝回调,sign:{},trade_status:{},参数:{}", params.get("sign"), params.get("trade_status"),
                 params.toString());

        // todo 根据官方文档验证签名

        //todo 非常重要：根据官方文档需要移除 sign ,sign_type 两个参数,但支付宝提供的源码里只移除了sign,故需要我们自己移除 sign_type，否则付款成功却验签会失败
        params.remove("sign_type");

//        try {
////            boolean alipayRSACheckV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
////            if(!alipayRSACheckV2){
////                return ServerResponse.createByErrorMessage("非法请求，验证不通过，再恶意请求将报警找网警");
////            }
//        } catch (AlipayApiException e) {
//            log.error("支付宝验证回调异常",e);
//        }

        //验证业务数据
//        ServerResponse response = orderService.alipayCallback(params);
//        if (response.isSuccess()) {
//            return Const.AlipayCallback.RESPONSE_SUCCESS;
//        }
        return AlipayConstants.RESPONSE_FAILED;
    }

    @GetMapping("/query_order_pay_status")
    public Map<String, Boolean> queryOrderPayStatus(Long orderNo, HttpServletRequest request) {
        boolean orderPaid = orderService.isOrderPaid(getUserId(request), orderNo);
        return Collections.singletonMap("paid", orderPaid);
    }

    private Integer getUserId(HttpServletRequest request) {
        return authService.getUserId(request);
    }
}
