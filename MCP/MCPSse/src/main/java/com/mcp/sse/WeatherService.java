package com.mcp.sse;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * @author Sleck
 */
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
}
