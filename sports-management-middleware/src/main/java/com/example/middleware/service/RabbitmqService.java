package com.example.middleware.service;

import com.example.middleware.pojo.Test;
import com.example.middleware.utils.RabbitMQUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RabbitmqService {

    private static final String TEST_QUEUE = "test-queue";
    private static final String TEST_MESSAGE = "Test message";
    private  static  final Test  test=new Test();
    @Autowired
    private RabbitMQUtil rabbitMQUtil;
    public void sendMessage(){
            String massage =rabbitMQUtil.toJsonString(test);
            log.info(massage+"???");
            rabbitMQUtil.sendMessage(massage,TEST_QUEUE);
//        TEST_QUEUE队列名非必要可以直接使用配置加载的队列名，当前开启了不存在的队列也自动创建的测试配置

    }
    public void receiveMessage(){
       String message= rabbitMQUtil.receiveMessage(5, TimeUnit.SECONDS,TEST_QUEUE);
        Test test1=rabbitMQUtil.toObject(message, com.example.middleware.pojo.Test.class);
        log.info(test1.toString());
    }

}
