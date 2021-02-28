package com.dlq.mall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.dlq.mall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private  String app_id = "2021000117614561";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDteX2KTH2mJ+OhEdw1QVOc7JfGdRxgX58uUJEb4iK4B5w1aTI20F+bBw9fb1r/Yd2xpcmav8Z3XxtNxeHk6n4kSiJgbPIXAReyO7hHKoLtOK4Y4d81kCYw6bCrbtlLu0Cv2H6dXUyZu6LCD4bkLPpLAIaYXR1x37m5s1BGaKCCwFdBYlWrfY8rSHq/Z3bgNdjYb+dLbaVZSnZGwwvVJ1rCTF5GH4A+ix7gWeEyIg9T/X1OlgrsmJo5Te1lTJs5YV+AppgxPX3sIub6gHsCOCSd38Gyvwew8cn6kbT+wvggz1pw0QSouddkLwFr98aUNrCURn54cVnu/rhO1RiceZSNAgMBAAECggEBAOtqmgqlI5a5So6djwj4VvRlE105McVUZhBa7E+REUwQt0m3nID8dxrPl+zLDpHHqif3K3IOlag3D3E9L2BpGl3NZGDk/+XQPnaBf4CFYz/aBbVDDa0i1sl8kZiRjjbmTg1E+Nf16v8oJQRushG3iPUbc1LOVGdUG+E9IuJ5zE1wiOWWcnlBbtK6Fqn2pnjQWrZMM+Zr94wLsLDeVmVZjiiHCPPCru+GgMtTUwKY+MCT5QuF/nTUR7p5A7ol0j0zFBuGCN7hqNs8RXkYR2OyeCtgyjowxacywvA3eWxmv36+3KTaOrku40PLjY4rOhIQiyfGQj28yUCwdjM9bi8QvMECgYEA+j9MqQti3Rv0pmsGxnolOpLg2EJtZpUZYc0YG4Q1mr89wUWNx6vVOQxB11tTIZPh+qjtGvwY26P1I5TYz0wXTXvMCQqkYrExQbC+L1ic3fM/lVq36PbYFwvId3SchjJpduDooSq/h+4ebQd9JzBxUU8zokYvoHgBRhgyPguYgFECgYEA8u8GHRmDsYkBame4NtFRVxhC0PjqwTMRapvhsFCA0mKrL03PKPRo2NqHK6TCF/kTCUFgyioCYl9cecgHl+MB+wtd14b8DqcxRYA9NWlZGDfnHuA3wp0dFUqoTvXb1MbU5gqvcs15xlOmii4jfCdwIUBlFUq34Ck4fBXMfl4t3X0CgYA8SAFEe1yuXKybZg60wvmy2WzWS3IlEueRWqFjB38OLuFGbGXR4n+zhVNa+EHXRdNa2VR8epvWVPGZR6LOlMrMPHUS/IBK0dpGs7esmhD3mkAHz1mcAdJeAtWgiPOnk/85xsXjBPQsX1zu8K3iQdYaOxT6EyQn6KCN7Qg1T//FcQKBgBbGUcxol/AM6BURpDN/bCC+JYVx0JBMOVXFR2NAKNOQbHwCz2kHgMzTr9hnmcqljyNO/cP2LEMMrZ69IfkbTxQ5/JI4FWlKQ1RAsXxQZAnhM8mxO4kDMbbNSPoeEn3gJhVq6wfYO5JcRUCvEqSRDfIQF2nnl1Vsh98aLBGswqABAoGABBqD+w+DNqcuOv9ewtS9qEal4llkPJM+OVytDWHqFsvGruYLsYrrcJV5nHlxdXFbppRW8wHOx7yflsHycss9Uv3pfWRa6KlApcSsKv2xbk0F9objhoLYbSRz+Y345tidVWb45TSW5JRgnn4teCOF0ZLmzvWgegpQNFB9zYfLwX0=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAs1UY5zEiUGtxyS79fyEx0DlihoFy8ElAyzZhpBLDpauRJdmYWG9e6KkhSbvgFyro3CldYQ0+v1/kAZCHIs75so0tRSJzpBIBM3Lu7Na1tu8/NJs/3l+DFHTDmXEpx9CH6l7yAqJrs8wEjYdPzT42yogBTfM5LPF8itqKUpKlHs6fcnL/0XSKu8523oO3cwAIocts+HBF+hzgBEue2UOhK7h+W2aaC5uC7dJPix0yQjAexQlJ+f0MVXlWKUEIqhy237m+k8j7PP27IHIkzRGj0AGb9EPTXxFs4ajq8D5EgZCQZSkRwvL9FoFZE6Yp3SW+SCvmBGK2JfQszfxfazeKbwIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url="http://order.dlqk8s.top:81/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url="http://member.dlqk8s.top:81/list.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    private String timeout_express = "30m";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+timeout_express+"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }

}
