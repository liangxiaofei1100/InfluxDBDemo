package com.epoo.kettle;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class InfluxDbClient {

    private InfluxDB influxDB;

    private String url;
    private String username;
    private String password;

    public static void main(String[] args) throws Exception {
        InfluxDbClient influxDbClient = new InfluxDbClient();
        influxDbClient.getConfig();
        influxDbClient.init();

//        influxDbClient.write();
        influxDbClient.read();

        influxDbClient.close();
    }

    public void getConfig() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("influxdb.properties"));
            url = properties.getProperty("influxdb/url");
            username = properties.getProperty("influxdb/username");
            password = properties.getProperty("influxdb/password");
        } catch (Exception e) {
            e.printStackTrace();
            url = "http://192.168.0.138:8086";
            username = "root";
            password = "root";
        }
    }

    public void init() {
        // Create an object to handle the communication with InfluxDB.
        // (best practice tip: reuse the 'influxDB' instance when possible)
        influxDB = InfluxDBFactory.connect(url, username, password);

        String databaseName = "epoo_cloud_data";
        influxDB.setDatabase(databaseName);
    }

    public void close() {
        influxDB.close();
    }

    public void write() {
        String enterprise_code = "yc1";
        String heat_source_name = "调峰热源";
        String heat_source_id = "28";
        String heat_source_code = "T002";
        Float supply_t = 50f;
        Float back_t = 48f;
        Float supply_f = 707f;
        Float supply_h = 824f;
        Double supply_h_total = 1952490d;

        // Write points to InfluxDB.
        influxDB.write(Point.measurement("heat_source")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("enterprise_code", enterprise_code)
                .tag("heat_source_name", heat_source_name)
                .tag("heat_source_id", heat_source_id)
                .tag("heat_source_code", heat_source_code)
                .addField("supply_t", supply_t)
                .addField("back_t", back_t)
                .addField("supply_f", supply_f)
                .addField("supply_h", supply_h)
                .addField("supply_h_total", supply_h_total)
                .build());
    }

    public void read() {
        // Query your data using InfluxQL.
        // https://docs.influxdata.com/influxdb/v1.7/query_language/data_exploration/#the-basic-select-statement

        LocalDateTime localDateTime = LocalDateTime.now().minusHours(1);
        LocalDateTime lastHourStart = LocalDateTime.of(localDateTime.toLocalDate(), LocalTime.of(localDateTime.getHour(), 0));
        LocalDateTime lastHourEnd = LocalDateTime.of(localDateTime.toLocalDate(), LocalTime.of(localDateTime.getHour(), 59, 59, 999));
        Date startTime = Date.from(lastHourStart.atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(lastHourEnd.atZone(ZoneId.systemDefault()).toInstant());
        String influxSql = "SELECT time, back_f, back_f_total, back_t, enterprise_code, heat_source_code, heat_source_id, " +
                "heat_source_name, supply_f, supply_f_total, supply_h, supply_h_total, supply_t " +
                "FROM heat_source where time >= " + startTime.getTime() + "ms AND time <= " + endTime.getTime() + "ms "
                + "order by time desc ";
        List<List<Object>> columnValues = getColumnValues(influxSql);
        System.out.println(columnValues.size());
        for (List<Object> values : columnValues) {
            String time = (String) values.get(0);
            Double back_f = (Double) values.get(1);
            Double back_f_total = (Double) values.get(2);
            Double back_t = (Double) values.get(3);
            String enterprise_code = (String) values.get(4);
            System.out.println("values: " + values);
        }
        // It will print something like:
        // QueryResult [results=[Result [series=[Series [name=h2o_feet, tags=null,
        //      columns=[time, level description, location, water_level],
        //      values=[
        //         [2020-03-22T20:50:12.929Z, below 3 feet, santa_monica, 2.064],
        //         [2020-03-22T20:50:12.929Z, between 6 and 9 feet, coyote_creek, 8.12]
        //      ]]], error=null]], error=null]


    }

    public List<List<Object>> getColumnValues(String sql) {
        List<List<Object>> columnValues = new ArrayList<List<Object>>();
        QueryResult queryResult = influxDB.query(new Query(sql));

        System.out.println(queryResult);
        List<QueryResult.Result> results = queryResult.getResults();
        if (results != null) {
            for (QueryResult.Result result : results) {
                List<QueryResult.Series> series = result.getSeries();
                if (series != null) {
                    for (QueryResult.Series serial : series) {
                        System.out.println("serialColumns： " + serial.getColumns());
                        List<List<Object>> serialValues = serial.getValues();
                        columnValues.addAll(serialValues);
                    }
                }
            }
        }
        Instant.parse("2007-12-03T10:15:30.00Z");

        return columnValues;
    }
}
