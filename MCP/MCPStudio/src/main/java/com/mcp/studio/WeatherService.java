package com.mcp.studio;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class WeatherService {
    @Tool(name = "getWeather", description = "获取天气信息")
    public String getWeather(@ToolParam( description = "城市名称") String city) {
        return "今天" + city + "的天气是晴天，温度是25度。";
    }
}
