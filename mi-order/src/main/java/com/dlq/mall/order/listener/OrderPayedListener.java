package com.dlq.mall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.dlq.mall.order.config.AlipayTemplate;
import com.dlq.mall.order.service.OrderService;
import com.dlq.mall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-28 16:40
 */
@RestController
public class OrderPayedListener {

    @Autowired
    OrderService orderService;
    @Autowired
    AlipayTemplate alipayTemplate;

    @PostMapping("/payed/notify")
    public String handleAlipayed(PayAsyncVo payAsyncVo,  HttpServletRequest request) throws AlipayApiException, UnsupportedEncodingException {
        //只要我们收到了支付宝给我们的异步通知，告诉我们订单支付成功。就返回success，支付宝就再也不通知
        /*Map<String, String[]> map = request.getParameterMap();
        System.out.println("支付宝通知到位了...数据：" + map);*/

        //重要环节：：：验签
        //获取支付宝POST过来反馈信息
        Map<String,String> params = new HashMap<String,String>();
        Map<String,String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(), alipayTemplate.getCharset(), alipayTemplate.getSign_type()); //调用SDK验证签名
        if (signVerified){
            //签名验证成功
            System.out.println("签名验证成功");
            return orderService.handlePayResult(payAsyncVo);
        }else {
            return "error";
        }

    }
}
