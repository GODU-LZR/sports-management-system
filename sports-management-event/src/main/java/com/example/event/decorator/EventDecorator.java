package com.example.event.decorator;

import com.example.event.entity.SportEvent;
import lombok.NoArgsConstructor;

/**
 * 赛事装饰器
 * 由于SportEvent是抽象类，这里应该使用extends而不是implements
 */
@NoArgsConstructor
public abstract class EventDecorator extends SportEvent {
    protected SportEvent decoratedEvent;
    
    public EventDecorator(SportEvent decoratedEvent) {
        this.decoratedEvent = decoratedEvent;
    }
    
    // 委托方法
    @Override
    public Long getId() {
        return decoratedEvent.getId();
    }
    
    @Override
    public void setId(Long id) {
        decoratedEvent.setId(id);
    }
    
    @Override
    public String getName() {
        return decoratedEvent.getName();
    }
    
    @Override
    public void setName(String name) {
        decoratedEvent.setName(name);
    }
    
    // 其他委托方法需要根据SportEvent类中的方法一一实现
    
    @Override
    public String getSportType() {
        return decoratedEvent.getSportType();
    }
}

