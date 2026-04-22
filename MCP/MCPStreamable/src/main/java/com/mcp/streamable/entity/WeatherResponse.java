package com.mcp.streamable.entity;

import lombok.Data;

/**
 * @author Sleck
 */
@Data
public class WeatherResponse {

    private String city;
    private String date;

    private String i;

    private String s;

    private String description;
    private double temperature;

    public WeatherResponse(String city, String date,String i,String s, String description, double temperature) {
        this.city = city;
        this.date = date;
        this.i = i;
        this.s = s;
        this.description = description;
        this.temperature = temperature;
    }
}
