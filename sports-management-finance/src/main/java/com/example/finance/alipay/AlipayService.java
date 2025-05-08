package com.example.finance.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class AlipayService {

    private final AlipayProperties alipayProperties;

    @Autowired
    public AlipayService(AlipayProperties alipayProperties) {
        this.alipayProperties = alipayProperties;
    }

    public AlipayClient getAlipayClient() {

        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayProperties.getServerUrl(),
                alipayProperties.getAppId(),
                alipayProperties.getPrivateKey(),
                alipayProperties.getFormat(),
                alipayProperties.getCharset(),
                alipayProperties.getAlipayPublicKey(),
                alipayProperties.getSignType()
        );
        System.out.println(alipayProperties.toString());
        return alipayClient;
    }
    /**
     * 查询支付宝订单是否支付完成
     *
     * @param outTradeNo 商户订单号
     * @return true 如果订单已支付成功，false 否则
     */
    public boolean isOrderPaid(String outTradeNo) {
        log.info("【订单状态判断】开始查询订单 {} 是否支付完成", outTradeNo);
        try {
            AlipayTradeQueryResponse response = executeAlipayTradeQuery(outTradeNo);
            if (response.isSuccess() && "TRADE_SUCCESS".equals(response.getTradeStatus())) {
                log.info("【订单状态判断】订单 {} 已支付成功", outTradeNo);
                return true;
            } else {
                if (response.isSuccess()) {
                    log.info("【订单状态判断】订单 {} 尚未支付成功，当前状态: {}", outTradeNo, response.getTradeStatus());
                } else {
                    log.warn("【订单状态判断】订单 {} 查询失败，错误码: {}, 错误信息: {}", outTradeNo, response.getSubCode(), response.getSubMsg());
                }
                return false;
            }
        } catch (AlipayApiException e) {
            log.error("【订单状态判断】订单 {} 查询异常: {}", outTradeNo, e.getMessage(), e);
            return false;
        }
    }
    /**
     * 查询支付宝订单状态
     *
     * @param outTradeNo 商户订单号
     * @return 包含订单状态信息的字符串，查询失败或异常时返回相应的错误信息
     */
    public String queryOrderStatus(String outTradeNo) {
        log.info("【订单查询】开始查询订单，商户订单号: {}", outTradeNo);

        try {
            AlipayTradeQueryResponse response = executeAlipayTradeQuery(outTradeNo);

            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                String totalAmount = response.getTotalAmount();
                String buyerLogonId = response.getBuyerLogonId();

                String result = String.format("订单 %s 状态为：%s，金额：%s，买家账号：%s", outTradeNo, tradeStatus, totalAmount, buyerLogonId);
                log.info("【订单查询】订单 {} 查询成功，状态: {}, 金额: {}, 买家账号: {}", outTradeNo, tradeStatus, totalAmount, buyerLogonId);
                return result;
            } else {
                log.warn("【订单查询】订单 {} 查询失败，错误码: {}, 错误信息: {}", outTradeNo, response.getSubCode(), response.getSubMsg());
                return "查询失败：" + response.getSubMsg();
            }

        } catch (AlipayApiException e) {
            log.error("【订单查询】订单 {} 查询异常: {}", outTradeNo, e.getMessage(), e);
            return "查询异常：" + e.getMessage();
        }
    }
    /**
     * 执行支付宝交易查询
     *
     * @param outTradeNo 商户订单号
     * @return AlipayTradeQueryResponse
     * @throws AlipayApiException 当与支付宝接口交互发生异常时抛出
     */
    private AlipayTradeQueryResponse executeAlipayTradeQuery(String outTradeNo) throws AlipayApiException {
        // 获取 AlipayClient 实例
        AlipayClient alipayClient = getAlipayClient();

        // 创建查询请求
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{\"out_trade_no\":\"" + outTradeNo + "\"}");

        // 执行查询并返回响应
        return alipayClient.execute(request);
    }
}