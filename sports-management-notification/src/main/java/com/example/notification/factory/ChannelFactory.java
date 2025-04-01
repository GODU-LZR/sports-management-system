package com.example.notification.factory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Channel工厂类，用于创建和管理RabbitMQ的Channel对象
 */
@Slf4j
public class ChannelFactory extends BasePooledObjectFactory<Channel> {
    private final Connection connection;

    public ChannelFactory(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Channel create() throws Exception {
        Channel channel = connection.createChannel();
        channel.confirmSelect(); // 开启发布确认模式
        log.debug("创建新的Channel: {}", channel);
        return channel;
    }

    @Override
    public PooledObject<Channel> wrap(Channel channel) {
        return new DefaultPooledObject<>(channel);
    }

    @Override
    public void destroyObject(PooledObject<Channel> pooledObject) {
        Channel channel = pooledObject.getObject();
        try {
            if (channel.isOpen()) {
                channel.close();
                log.debug("关闭Channel: {}", channel);
            }
        } catch (Exception e) {
            log.error("关闭Channel失败", e);
        }
    }

    @Override
    public boolean validateObject(PooledObject<Channel> pooledObject) {
        Channel channel = pooledObject.getObject();
        return channel != null && channel.isOpen();
    }
}