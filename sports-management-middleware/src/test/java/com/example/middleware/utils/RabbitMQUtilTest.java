package com.example.middleware.utils;

import com.example.middleware.service.RabbitmqService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RabbitMQUtilTest {



    @Autowired
    private RabbitMQUtil rabbitMQUtil;
    @Autowired
    private RabbitmqService rabbitmqService;
    private static final String TEST_QUEUE = "test-queue";
    private static final String TEST_MESSAGE = "Test message";

    @BeforeEach
    void setUp() {
        rabbitMQUtil.setQueueName(TEST_QUEUE);
    }

    @AfterEach
    void tearDown() {
        // 清理测试队列中的消息
        String message;
        while ((message = rabbitMQUtil.receiveMessage(100, TimeUnit.MILLISECONDS)) != null) {
            // 消费掉所有消息
        }
    }

    @Test
    void testSendAndReceiveMessage() throws InterruptedException {
        // 发送消息
        rabbitMQUtil.sendMessage(TEST_MESSAGE);

        // 使用CountDownLatch等待消息接收
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> receivedMessage = new AtomicReference<>();

        // 接收消息
        rabbitMQUtil.receiveMessage(message -> {
            receivedMessage.set(message);
            latch.countDown();
        });

        // 等待消息接收，最多等待5秒
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(TEST_MESSAGE, receivedMessage.get());
    }

    @Test
    void testSyncReceiveMessage() {
        // 发送消息
        rabbitMQUtil.sendMessage(TEST_MESSAGE);

        // 同步接收消息
        String receivedMessage = rabbitMQUtil.receiveMessage(5, TimeUnit.SECONDS);
        assertEquals(TEST_MESSAGE, receivedMessage);
    }

    @Test
    void testService(){
        rabbitmqService.sendMessage();
        rabbitmqService.receiveMessage();
    }
//
//    @Test
//    void testMessageProcessingFailureAndRetry() throws InterruptedException {
//        // 发送消息
//        rabbitMQUtil.sendMessage(TEST_MESSAGE);
//
//        CountDownLatch latch = new CountDownLatch(2);
//        AtomicReference<Integer> processCount = new AtomicReference<>(0);
//
//        // 模拟消息处理失败和重试
//        MessageCallback failingCallback = message -> {
//            int count = processCount.get();
//            processCount.set(count + 1);
//            latch.countDown();
//
//            if (count == 0) {
//                throw new RuntimeException("模拟处理失败");
//            }
//        };
//
//        rabbitMQUtil.receiveMessage(failingCallback);
//
//        // 等待消息处理和重试
//        assertTrue(latch.await(10, TimeUnit.SECONDS));
//        assertEquals(2, processCount.get());
//    }
//
//    @Test
//    void testDeadLetterQueue() throws InterruptedException {
//        // 发送消息
//        rabbitMQUtil.sendMessage(TEST_MESSAGE);
//
//        CountDownLatch latch = new CountDownLatch(1);
//        AtomicReference<String> deadLetterMessage = new AtomicReference<>();
//
//        // 设置一个始终失败的消息处理器
//        MessageCallback alwaysFailingCallback = message -> {
//            throw new RuntimeException("始终失败");
//        };
//
//        // 接收死信队列消息
//        rabbitMQUtil.setQueueName(TEST_QUEUE + ".dlq");
//        rabbitMQUtil.receiveMessage(message -> {
//            deadLetterMessage.set(message);
//            latch.countDown();
//        });
//
//        // 处理原始队列消息（会失败并进入死信队列）
//        rabbitMQUtil.setQueueName(TEST_QUEUE);
//        rabbitMQUtil.receiveMessage(alwaysFailingCallback);
//
//        // 等待消息进入死信队列
//        assertTrue(latch.await(10, TimeUnit.SECONDS));
//        assertEquals(TEST_MESSAGE, deadLetterMessage.get());
//    }
}