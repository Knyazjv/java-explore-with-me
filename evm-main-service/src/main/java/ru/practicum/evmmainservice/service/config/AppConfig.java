package ru.practicum.evmmainservice.service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.practicum.statsclient.StatsClient;

@Import(value = StatsClient.class)
@Configuration
public class AppConfig {
}

