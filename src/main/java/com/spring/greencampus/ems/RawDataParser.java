package com.spring.greencampus.ems;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class RawDataParser {
    public static void main(String[] args) {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "rawdata-parser-application");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:7092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Integer().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        HashMap<Integer, SensorData> map = new HashMap<>();

        KStreamBuilder builder = new KStreamBuilder();
        // 1 - stream from Kafka
        ObjectMapper mapper = new ObjectMapper();

        KStream<Integer, String> keypair = builder.stream("raw-data-input");
        KStream<Integer, String> dataStream = keypair.filter((key, value) -> value.contains(","))
                .map((key, value) -> {
                    if (!map.containsKey(key)) {
                        map.put(key, new SensorData(key, new Date()));
                    }
                    SensorData sensorData = map.get(key);
                    String[] strs = value.split(",");
                    if (strs[0].equals("0")) {
                        sensorData.setVoltage(10 * Float.valueOf(strs[1]));
                        sensorData.setCurrent(2 * Float.valueOf(strs[3]));
                    } else if (strs[0].equals("2")) {
                        sensorData.setTemperature(50 * Float.valueOf(strs[1]));
                    } else if (strs[0].equals("3")) {
                        sensorData.setHumidity(100 * Float.valueOf(strs[1]));
                    }
                    sensorData.setTimestamp(new Date());

                    CloseableHttpClient client = HttpClients.createDefault();
                    try {

                        List<SensorData> list = new ArrayList<>();
                        list.add(sensorData);
                        String jsonbody = mapper.writeValueAsString(list);
                        HttpPost post = new HttpPost("http://localhost:9191/sensordata");
                        post.addHeader("content-type", "application/json;charset=UTF-8");
                        StringEntity body = new StringEntity(jsonbody);
                        post.setEntity(body);
                        HttpResponse response = client.execute(post);
                        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                            return KeyValue.pair(key, jsonbody);
                        } else {
                            return KeyValue.pair(key, "");
                        }

                    } catch (JsonProcessingException e) {
                        return KeyValue.pair(key, sensorData.toString());
                    } catch (UnsupportedEncodingException e) {
                        return KeyValue.pair(key, sensorData.toString());
                    } catch (ClientProtocolException e) {
                        return KeyValue.pair(key, sensorData.toString());
                    } catch (IOException e) {
                        return KeyValue.pair(key, sensorData.toString());
                    }
                    finally {
                        try {
                            client.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .filter((key, value) -> !value.equals(""));
        // 7 - to in order to write the results back to kafka
        dataStream.to(Serdes.Integer(), Serdes.String(), "sensor-data-insertion-failed");

        KafkaStreams streams = new KafkaStreams(builder, config);
        streams.start();

        // print the topology
        System.out.println(streams.toString());

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

}
