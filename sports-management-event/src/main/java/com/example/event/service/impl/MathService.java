package com.example.event.service.impl;

import com.example.event.service.MatchService;
import org.springframework.stereotype.Service;

@Service
public class MathService implements MatchService {
    @Override
    public boolean existsById() {
        return false;
    }
}
