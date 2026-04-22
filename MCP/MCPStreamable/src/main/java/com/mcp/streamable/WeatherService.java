package com.mcp.streamable;

import com.alibaba.fastjson2.JSON;
import com.mcp.streamable.entity.WeatherRequest;
import com.mcp.streamable.entity.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * @author Sleck
 */
@Service
@Slf4j
public class WeatherService {
    @Tool(name = "getWeather", description = "获取天气信息")
    public String getWeather(@ToolParam( description = "城市名称") String city) {
        return switch (city) {
            case "上海" -> "今天上海的天气是晴天，温度是25度。";
            case "北京" -> "今天北京的天气是晴天，温度是25度。";
            case "广州" -> "今天广州的天气是晴天，温度是25度。";
            default -> "没有找到该城市的天气信息。";
        };
    }

    @Tool(
            name = "query_weather_by_city&date",
            description = "根据城市和日期获取天气信息"
    )
    public WeatherResponse queryWeather(WeatherRequest request) {
        log.info("query weather for city:{}", JSON.toJSONString(request));
        try {
            // 模拟调用api
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        double temp = Math.random() * 15 + 10;

        return new WeatherResponse(
                request.getCity(),
                request.getDate(),
                request.getI(),
                request.getS(),
                "晴朗，有微风",
                temp
        );
    }
}
