package com.example.finance.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@Slf4j
@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class AlipayCallbackController {
    private final AlipayService alipayService;
    private final AlipayProperties alipayProperties;
    private final AlipayUtils alipayUtils;

    @GetMapping("/query")
    public String queryOrderStatusC(@RequestParam("outTradeNo") String outTradeNo) {
        System.out.println("【订单查询】开始查询订单：" + outTradeNo);

        try {
            // 初始化 AlipayClient
            AlipayClient alipayClient = alipayService.getAlipayClient();

            // 创建查询请求
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            request.setBizContent("{" +
                    "\"out_trade_no\":\"" + outTradeNo + "\"" +
                    "}");

            // 执行查询
            AlipayTradeQueryResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                String totalAmount = response.getTotalAmount();
                String buyerLogonId = response.getBuyerLogonId();

                System.out.println("【订单查询】状态: " + tradeStatus);
                return String.format("订单 %s 状态为：%s，金额：%s，买家账号：%s", outTradeNo, tradeStatus, totalAmount, buyerLogonId);
            } else {
                return "查询失败：" + response.getSubMsg();
            }

        } catch (AlipayApiException e) {
            e.printStackTrace();
            return "查询异常：" + e.getMessage();
        }
    }

    // 同步回调（用户支付完成跳转回这个页面）
    @GetMapping("/return")
    public String returnUrlHandler(HttpServletRequest request) throws Exception {
        System.out.println("【同步回调】进入 returnUrlHandler");
        try {


            if (alipayUtils.verifyAlipaySignature(request, alipayProperties.getAlipayPublicKey(), alipayProperties.getCharset(), alipayProperties.getSignType())) {
                //唯一标识
                String outTradeNo = request.getParameter("out_trade_no");
                String tradeNo = request.getParameter("trade_no");
                String tradeStatus = request.getParameter("trade_status");

                System.out.println("订单号：" + outTradeNo);
                System.out.println("交易状态：" + tradeStatus);

                if (alipayService.isOrderPaid(outTradeNo)) {
                    return "支付成功，请关闭页面";
                } else {
                    return "支付未完成";
                }
            }
        } catch (Exception e) {
            log.error("e: ", e);
            System.out.println("签名验证失败！请检查请求来源");
        }

        return "签名验证失败！请检查请求来源";

    }

    // 异步回调（支付宝服务器主动通知）
    @PostMapping("/notify")
    public String notifyUrlHandler(HttpServletRequest request) {
        System.out.println("【异步回调】进入 notifyUrlHandler");
        try {


            if (alipayUtils.verifyAlipaySignature(request, alipayProperties.getAlipayPublicKey(), alipayProperties.getCharset(), alipayProperties.getSignType())) {
                String outTradeNo = request.getParameter("out_trade_no");
                String tradeNo = request.getParameter("trade_no");
                String tradeStatus = request.getParameter("trade_status");

                System.out.println("订单号：" + outTradeNo);
                System.out.println("交易状态：" + tradeStatus);

                if ("TRADE_SUCCESS".equals(tradeStatus)) {
                    // 更新数据库订单状态为“已支付”
                    return "success"; // 必须返回 success，否则支付宝会重复通知
                }
            }
        } catch (Exception e) {
            log.error("e: ", e);
            System.out.println("failt");
        }
        return "fail";
    }





}