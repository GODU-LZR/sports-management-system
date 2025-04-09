package com.example.middleware.utils;

import com.example.middleware.pojo.TestCLASS1;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RabbitMQUtilTestCLASS1 {

    @Autowired
    private RabbitMQUtil rabbitMQUtil;

    private static final String TEST_QUEUE = "t1";
    private static final String TEST_MESSAGE = "TestCLASS1 message";
    private static final String OBJECT_TEST_QUEUE = "test-queue";
    private static final String TEST_EXCHANGE = "test-exchange";
    private static final String TEST_ROUTING_KEY = "test-routing";

    @BeforeEach
    void setUp() {
        rabbitMQUtil.setQueueName(TEST_QUEUE);
    }

    @AfterEach
    void tearDown() {
        // 清理测试队列中的消息
        // String message;
        // while ((message = rabbitMQUtil.receiveMessage()) != null) {
        //     // 消费掉所有消息
        // }
    }
    
    @Test
    void testSendMessage() throws InterruptedException {
        rabbitMQUtil.sendMessage("cnm我是测试让我测你码");
    }
    
    @Test
    void testReceiveMessage() throws InterruptedException {
        String message = rabbitMQUtil.receiveMessage();
        System.out.println(message + "????????");
    }
    
    /**
     * 测试发送自定义对象
     */
    @Test
    void testSendObjectMessage() {
        // 创建测试对象
        TestCLASS1 testObj = new TestCLASS1();
        
        // 使用sendObject方法直接发送对象
        rabbitMQUtil.sendObject(testObj, OBJECT_TEST_QUEUE);
        System.out.println("已发送TestCLASS1对象到队列: " + OBJECT_TEST_QUEUE);
    }
    
    /**
     * 测试接收并解析自定义对象
     */
    @Test
    void testReceiveObjectMessage() {
        // 设置队列名
        rabbitMQUtil.setQueueName(OBJECT_TEST_QUEUE);
        
        // 直接使用receiveObject方法接收对象
        TestCLASS1 testObj = rabbitMQUtil.receiveObject(TestCLASS1.class);
        
        if (testObj != null) {
            System.out.println("接收到的对象: " + testObj.toString());
            assertNotNull(testObj);
        } else {
            System.out.println("未接收到对象，队列可能为空");
        }
    }
    
    /**
     * 测试使用交换机和路由键发送对象
     */
    @Test
    void testSendObjectWithExchange() {
        // 创建测试对象
        TestCLASS1 testObj = new TestCLASS1();
        
        // 确保交换机和队列存在
        rabbitMQUtil.createExchangeAndBinding(TEST_EXCHANGE, OBJECT_TEST_QUEUE, TEST_ROUTING_KEY, "direct");
        
        // 发送对象到交换机
        rabbitMQUtil.sendObjectWithExchange(TEST_EXCHANGE, TEST_ROUTING_KEY, testObj);
        System.out.println("已通过交换机发送对象: " + TEST_EXCHANGE + ", 路由键: " + TEST_ROUTING_KEY);
    }
    
    /**
     * 测试使用Direct交换机发送对象(简化版)
     */
    @Test
    void testSendObjectWithDirectExchange() {
        // 创建测试对象
        TestCLASS1 testObj = new TestCLASS1();
        
        // 使用简化方法发送对象
        rabbitMQUtil.sendObjectWithDirectExchange(testObj, TEST_EXCHANGE, TEST_ROUTING_KEY);
        System.out.println("已通过Direct交换机发送对象: " + TEST_EXCHANGE + ", 路由键: " + TEST_ROUTING_KEY);
    }
    
    /**
     * 测试发送带类型标识的对象
     */
    @Test
    void testSendTypedObject() {
        // 创建测试对象
        TestCLASS1 testObj = new TestCLASS1();
        
        // 发送带类型标识的对象
        rabbitMQUtil.sendTypedObject(testObj, "TestCLASS1", OBJECT_TEST_QUEUE);
        System.out.println("已发送带类型标识的对象到队列: " + OBJECT_TEST_QUEUE);
    }
    
    /**
     * 测试接收指定类型的对象
     */
    @Test
    void testReceiveTypedObject() {
        // 接收指定类型的对象
        TestCLASS1 testObj = rabbitMQUtil.receiveTypedObject("TestCLASS1", TestCLASS1.class);
        
        if (testObj != null) {
            System.out.println("接收到类型为TestCLASS1的对象: " + testObj.toString());
            assertNotNull(testObj);
        } else {
            System.out.println("未接收到类型为TestCLASS1的对象，队列可能为空或没有匹配类型的消息");
        }
    }
    
    /**
     * 测试发送不同类型的对象到同一队列
     */
    @Test
    void testSendMixedTypedObjects() {
        // 创建测试对象
        TestCLASS1 testObj1 = new TestCLASS1();
        TestCLASS1 testObj2 = new TestCLASS1();
        
        // 发送不同类型的对象
        rabbitMQUtil.sendTypedObject(testObj1, "TestCLASS1", OBJECT_TEST_QUEUE);
        rabbitMQUtil.sendTypedObject(testObj2, "OtherType", OBJECT_TEST_QUEUE);
        
        System.out.println("已发送不同类型的对象到队列: " + OBJECT_TEST_QUEUE);
    }
    
    /**
     * 测试接收并筛选特定类型的对象
     */
    @Test
    void testReceiveAndFilterTypedObjects() {
        // 尝试接收5次，找到指定类型的对象
        TestCLASS1 testObj = rabbitMQUtil.receiveTypedObject("TestCLASS1", TestCLASS1.class, 5, 5000);
        
        if (testObj != null) {
            System.out.println("成功筛选并接收到类型为TestCLASS1的对象: " + testObj.toString());
            assertNotNull(testObj);
        } else {
            System.out.println("未找到类型为TestCLASS1的对象");
        }
    }
    
    /**
     * 测试完整的消息发送和接收流程
     */
    @Test
    void testCompleteMessageFlow() {
        // 1. 创建测试对象
        TestCLASS1 testObj = new TestCLASS1();
        testObj.setUsername("测试用户");
        testObj.setEmail("test@example.com");
        
        // 2. 发送对象到队列
        rabbitMQUtil.sendObject(testObj, OBJECT_TEST_QUEUE);
        System.out.println("已发送对象到队列: " + OBJECT_TEST_QUEUE);
        
        // 3. 接收对象并验证
        TestCLASS1 receivedObj = rabbitMQUtil.receiveObject(OBJECT_TEST_QUEUE, TestCLASS1.class);
        
        if (receivedObj != null) {
            System.out.println("接收到的对象: " + receivedObj.toString());
            assertEquals("测试用户", receivedObj.getUsername());
            assertEquals("test@example.com", receivedObj.getEmail());
        } else {
            fail("未接收到对象");
        }
    }
    
    /**
     * 测试消息类型检查功能
     */
    @Test
    void testMessageTypeCheck() {
        // 创建测试对象
        TestCLASS1 testObj = new TestCLASS1();
        
        // 发送带类型标识的对象
        rabbitMQUtil.sendTypedObject(testObj, "TestCLASS1", OBJECT_TEST_QUEUE);
        
        // 接收消息但不解析
        String message = rabbitMQUtil.receiveMessage(OBJECT_TEST_QUEUE);
        
        if (message != null) {
            // 检查消息类型
            boolean isCorrectType = rabbitMQUtil.isMessageOfType(message, "TestCLASS1");
            assertTrue(isCorrectType, "消息类型应该是TestCLASS1");
            
            // 提取数据部分
            String dataJson = rabbitMQUtil.extractDataFromTypedMessage(message);
            assertNotNull(dataJson, "应该能够提取数据部分");
            
            // 手动解析对象
            TestCLASS1 extractedObj = rabbitMQUtil.toObject(dataJson, TestCLASS1.class);
            assertNotNull(extractedObj, "应该能够解析提取的数据");
            
            System.out.println("成功检查消息类型并提取数据: " + extractedObj);
        } else {
            fail("未接收到消息");
        }
    }
    
    /**
     * 测试使用不同路由键发送和接收消息
     */
    @Test
    void testMultipleRoutingKeys() {
        // 创建交换机和队列绑定
        String exchange = "multi-routing-exchange";
        String queue = "multi-routing-queue";
        String routingKey1 = "route1";
        String routingKey2 = "route2";
        
        // 创建绑定关系
        rabbitMQUtil.createExchangeAndBinding(exchange, queue, routingKey1, "direct");
        rabbitMQUtil.createExchangeAndBinding(exchange, queue, routingKey2, "direct");
        
        // 创建两个不同的测试对象
        TestCLASS1 obj1 = new TestCLASS1();
        obj1.setUsername("路由1用户");
        
        TestCLASS1 obj2 = new TestCLASS1();
        obj2.setUsername("路由2用户");
        
        // 使用不同的路由键发送对象
        rabbitMQUtil.sendObjectWithExchange(exchange, routingKey1, obj1);
        rabbitMQUtil.sendObjectWithExchange(exchange, routingKey2, obj2);
        
        System.out.println("已使用不同路由键发送两个对象");
        
        // 接收消息并验证
        TestCLASS1 received1 = rabbitMQUtil.receiveObject(queue, TestCLASS1.class);
        assertNotNull(received1, "应该能接收到第一个对象");
        
        TestCLASS1 received2 = rabbitMQUtil.receiveObject(queue, TestCLASS1.class);
        assertNotNull(received2, "应该能接收到第二个对象");
        
        System.out.println("成功接收两个对象: " + received1.getUsername() + ", " + received2.getUsername());
    }
    
    /**
     * 测试消息持久性
     */
    @Test
    void testMessagePersistence() {
        // 创建测试对象
        TestCLASS1 testObj = new TestCLASS1();
        testObj.setUsername("持久化测试用户");
        
        // 确保队列存在
        rabbitMQUtil.ensureQueueExists("persistent-queue");
        
        // 发送对象
        rabbitMQUtil.sendObject(testObj, "persistent-queue");
        System.out.println("已发送持久化对象到队列");
        
        // 接收并验证
        TestCLASS1 receivedObj = rabbitMQUtil.receiveObject("persistent-queue", TestCLASS1.class);
        
        if (receivedObj != null) {
            System.out.println("成功接收持久化对象: " + receivedObj.getUsername());
            assertEquals("持久化测试用户", receivedObj.getUsername());
        } else {
            fail("未接收到持久化对象");
        }
    }
}

