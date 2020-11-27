package com.epoo.kettle;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        // Create an object to handle the communication with InfluxDB.
        // (best practice tip: reuse the 'influxDB' instance when possible)
        final String serverURL = "http://192.168.0.138:8086", username = "root", password = "root";
        final InfluxDB influxDB = InfluxDBFactory.connect(serverURL, username, password);

        String databaseName = "epoo_cloud_data";
        influxDB.setDatabase(databaseName);

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

        // Close it if your application is terminating or you are not using it anymore.
        influxDB.close();
    }
}
