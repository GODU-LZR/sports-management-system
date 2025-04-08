package com.example.middleware.service;

import com.example.middleware.pojo.TestCLASS1;
import com.example.middleware.utils.RabbitMQUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitmqService {

    private static final String TEST_QUEUE = "test-queue";
    private static final String TEST_MESSAGE = "TestCLASS1 message";
    private  static  final TestCLASS1 test=new TestCLASS1();
    @Autowired
    private RabbitMQUtil rabbitMQUtil;
    public void sendMessage(){
            String massage =rabbitMQUtil.toJsonString(test);

            int i=0;while (i<10) {
            rabbitMQUtil.sendMessage(massage, TEST_QUEUE);
//        TEST_QUEUE队列名非必要可以直接使用配置加载的队列名，当前开启了不存在的队列也自动创建的测试配置
        i++;
            }
    }
    public void receiveMessage() {
        // 先检查队列状态

        
        // 尝试多次接收消息
        for (int i = 0; i < 3; i++) {
            String message = rabbitMQUtil.receiveMessage();
            if (message != null) {
                log.info("接收到的消息为: {}", message);
                TestCLASS1 test1 = rabbitMQUtil.toObject(message, TestCLASS1.class);
                log.info(test1.toString());
                return;
            } else {
                log.info("第{}次尝试未接收到消息", i + 1);
                try {
                    // 短暂等待后再次尝试
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        log.warn("多次尝试后仍未接收到消息，队列可能为空");
    }

}
