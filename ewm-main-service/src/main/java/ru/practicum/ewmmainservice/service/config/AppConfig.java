package ru.practicum.ewmmainservice.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.statsclient.StatsClient;


@Configuration
public class AppConfig {

    @Value("${ewm.stats.client.url}")
    private String statsClientUrl;

    @Bean
    public StatsClient statsClient() {
        return new StatsClient(statsClientUrl);
    }
}

