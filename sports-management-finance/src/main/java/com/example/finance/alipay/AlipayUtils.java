package com.example.finance.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Component
public class AlipayUtils {

    // 验证支付宝回调签名
    public boolean verifyAlipaySignature(HttpServletRequest request, String alipayPublicKey, String charset, String signType) throws AlipayApiException {
        Map<String, String> params = this.convertRequestParamsToMap(request);
        return AlipaySignature.rsaCheckV1(params, alipayPublicKey, charset, signType); // 使用rsaCheckV1或rsaCheckV2根据实际情况选择
    }

    // 将HttpServletRequest参数转换为Map
    private Map<String, String> convertRequestParamsToMap(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String paramName : requestParams.keySet()) {
            String[] paramValues = requestParams.get(paramName);
            if (paramValues.length == 0 || paramValues.length > 1) {
                map.put(paramName, paramValues[0]);
            } else {
                map.put(paramName, String.join(",", paramValues));
            }
        }
        return map;
    }
}