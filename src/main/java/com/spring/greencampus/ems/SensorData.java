package com.spring.greencampus.ems;

import java.util.Date;

public class SensorData {
    private int sensorId;
    private float voltage;
    private float current;
    private float temperature;
    private float humidity;
    private Date timestamp;

    public SensorData(int sensorId, Date timestamp) {
        this.sensorId = sensorId;
        this.timestamp = timestamp;
    }

    public SensorData(int sensorId, float voltage, float current, float temperature, float humidity, Date timestamp) {
        this.sensorId = sensorId;
        this.voltage = voltage;
        this.current = current;
        this.temperature = temperature;
        this.humidity = humidity;
        this.timestamp = timestamp;
    }

    public long getSensorId() {
        return sensorId;
    }

    public void setSensorId(int sensorId) {
        this.sensorId = sensorId;
    }

    public float getVoltage() {
        return voltage;
    }

    public void setVoltage(float voltage) {
        this.voltage = voltage;
    }

    public float getCurrent() {
        return current;
    }

    public void setCurrent(float current) {
        this.current = current;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "[{" +
                "\"sensorId\": " + sensorId +
                ", voltage=" + voltage +
                ", current=" + current +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", timestamp=" + String.valueOf(timestamp.getTime()) +
                '}';
    }


}
