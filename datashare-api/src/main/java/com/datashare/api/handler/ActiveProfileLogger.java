package com.datashare.api.handler;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ActiveProfileLogger {
    public ActiveProfileLogger(Environment environment) {
        log.info(
                "Active Spring profiles: {}",
                String.join(", ", environment.getActiveProfiles()));
    }
}
