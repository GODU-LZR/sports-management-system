package com.example.common.lock;

public interface LockHandle {
    String getResourceKey();
    // 可能包含其他信息，如锁的唯一标识符
}
