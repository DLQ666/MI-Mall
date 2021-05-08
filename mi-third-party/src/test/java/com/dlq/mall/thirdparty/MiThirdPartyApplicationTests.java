//package com.dlq.mall.thirdparty;
//
//import com.aliyun.oss.OSSClient;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.InputStream;
//
//@SpringBootTest
//class MiThirdPartyApplicationTests {
//
//    @Autowired
//    OSSClient ossClient;
//
//    @Value("${spring.cloud.alicloud.oss.endpoint}")
//    private String endpoint;
//
//    @Value("${spring.cloud.alicloud.oss.bucket}")
//    private String bucket;
//
//    @Test
//    public void testUpload() throws FileNotFoundException {
//        /*// Endpoint以杭州为例，其它Region请按实际情况填写。
//        String endpoint = "oss-cn-beijing.aliyuncs.com";
//        // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
//        String accessKeyId = "LTAI4GBwiRuPNBXdmn6CfDS7";
//        String accessKeySecret = "jybOLXwtMe05bGKoJQADEGjUkjQ4sM";
//
//        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);*/
//
//        // 上传文件流。
//        InputStream inputStream = new FileInputStream("D:\\Desktop\\HTTP状态码.jpg");
//        ossClient.putObject("dlq-mi-mall", "哈哈哈哈哈哈.jpg", inputStream);
//
//        // 关闭OSSClient。
//        ossClient.shutdown();
//
//        System.out.println("上传成功");
//    }
//
//    @Test
//    public void test0111() {
//        String url = "https://dlq-mi-mall.oss-cn-beijing.aliyuncs.com/2020-10-17/ae8204b7-598d-4e9c-8a61-32d9fc3e8193_2c0ba419dce57806d4080f3d1dd928a186ac989e5f2634b23c9a7633ab6ac07d.jpg";
//
//        String host = "https://" + bucket + "." + endpoint + "/";
//
//        System.out.println(host.length());
//
//        String objectName = url.substring(host.length());
//
//        System.out.println(objectName);
//    }
//
//}
