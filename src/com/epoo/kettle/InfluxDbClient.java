package com.epoo.kettle;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class InfluxDbClient {

    private InfluxDB influxDB;

    public void init() {
        // Create an object to handle the communication with InfluxDB.
        // (best practice tip: reuse the 'influxDB' instance when possible)
        final String serverURL = "http://192.168.0.138:8086", username = "root", password = "root";
        influxDB = InfluxDBFactory.connect(serverURL, username, password);

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

        Date oneHourAgo = new Date(System.currentTimeMillis() - 60 * 60 * 1000);
        Date now = new Date();
        String influxSql = "SELECT time, back_f, back_f_total, back_t, enterprise_code, heat_source_code, heat_source_id, " +
                "heat_source_name, supply_f, supply_f_total, supply_h, supply_h_total, supply_t " +
                "FROM heat_source where time >= " + oneHourAgo.getTime() + "ms AND time <= " + now.getTime() + "ms "
                + "order by time desc ";
        QueryResult queryResult = influxDB.query(new Query(influxSql));

        System.out.println(queryResult);
//        List<Result> results = queryResult.getResults();
        if (queryResult.getResults() != null) {
            int size = queryResult.getResults().size();
            for (int i = 0; i < size; i++) {
                Result result = queryResult.getResults().get(i);
                if (result.getSeries() != null) {
                    int serialSize = result.getSeries().size();
                    for (int j = 0; j < serialSize; j++) {
                        Series serial = result.getSeries().get(j);
                        String serialName = serial.getName();
                        System.out.println("serialName: " + serialName);
                        System.out.println("serialColumns： " + serial.getColumns());
                        int size2 = serial.getValues().size();
                        for (int k = 0; k < size2; k++) {
                            System.out.println("columnValues: " + serial.getValues().get(k));
                        }
                    }
                }
            }
        }

        // It will print something like:
        // QueryResult [results=[Result [series=[Series [name=h2o_feet, tags=null,
        //      columns=[time, level description, location, water_level],
        //      values=[
        //         [2020-03-22T20:50:12.929Z, below 3 feet, santa_monica, 2.064],
        //         [2020-03-22T20:50:12.929Z, between 6 and 9 feet, coyote_creek, 8.12]
        //      ]]], error=null]], error=null]
    }

    public static void main(String[] args) throws InterruptedException {
        InfluxDbClient influxDbClient = new InfluxDbClient();
        influxDbClient.init();

//        influxDbClient.write();
        influxDbClient.read();

        influxDbClient.close();
    }
}
