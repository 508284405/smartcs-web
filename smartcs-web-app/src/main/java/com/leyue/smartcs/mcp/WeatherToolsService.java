package com.leyue.smartcs.mcp;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WeatherToolsService {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(WeatherToolsService.class);

    private final RestClient restClient;

    public WeatherToolsService() {
        this.restClient = RestClient.create();
    }

    public record WeatherResponse(Current current) {
        public record Current(LocalDateTime time, int interval, double temperature_2m) {
        }
    }

    @Tool("获取指定位置的温度(摄氏度)")
    public String getTemperature(double latitude, double longitude) {
        WeatherResponse weatherResponse = restClient
                .get()
                .uri("https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m",
                        latitude, longitude)
                .retrieve()
                .body(WeatherResponse.class);

        return "当前温度: " + weatherResponse.current.temperature_2m + "°C";
    }
}