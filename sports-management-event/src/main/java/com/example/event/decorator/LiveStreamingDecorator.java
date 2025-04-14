package com.example.event.decorator;

import com.example.event.entity.SportEvent;
import lombok.Data;

// 直播功能装饰器

public class LiveStreamingDecorator extends EventDecorator {
    private String streamingUrl;

    public LiveStreamingDecorator(SportEvent decoratedEvent, String streamingUrl) {
        super(decoratedEvent);
        this.streamingUrl = streamingUrl;
    }

    public String getStreamingUrl() {
        return streamingUrl;
    }

    @Override
    public String getSportType() {
        return decoratedEvent.getSportType();
    }

    // 其他直播相关方法...
}
