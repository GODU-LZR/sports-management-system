package com.example.finance.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;

import com.example.common.utils.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay")
public class AlipayController {


    private final AlipayService alipayService;
    private SnowflakeIdGenerator generator=new SnowflakeIdGenerator();
    @Autowired
    public AlipayController(AlipayService alipayService) {
        this.alipayService = alipayService;
    }

    @GetMapping("/submit")
    public String submitPay() throws AlipayApiException {
        AlipayClient client = alipayService.getAlipayClient();

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        String outTradeNoId=String.valueOf(generator.nextId());
        System.out.println("outTradeNoId:"+outTradeNoId);
        model.setOutTradeNo(outTradeNoId);
        model.setTotalAmount("88.88");
        model.setSubject("测试商品");
        model.setProductCode("FAST_INSTANT_TRADE_PAY");

        request.setBizModel(model);
        request.setReturnUrl("http://113.45.197.3:19651/pay/return");
        request.setNotifyUrl("http://113.45.197.3:19651/pay/notify");

        return client.pageExecute(request).getBody();
    }
    @GetMapping("/testprint")
    public String submitPayP() throws AlipayApiException {
        AlipayClient client = alipayService.getAlipayClient();
        System.out.println(client.toString());
        return  null;
    }

}