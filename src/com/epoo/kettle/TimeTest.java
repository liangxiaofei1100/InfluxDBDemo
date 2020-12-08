package com.epoo.kettle;

import java.time.Instant;
import java.util.Date;

/**
 * @author liangxiaofei
 * @date 2020/12/2 9:02
 */
public class TimeTest {
    public static void main(String[] args) {
        String t1 = "2020-12-01T06:00:39.371Z";
        Instant instant = Instant.parse(t1);
        System.out.println(Date.from(instant));
        System.out.println(instant.toEpochMilli());

        String t2 = "2020-12-01T06:00:39.37Z";
        instant = Instant.parse(t2);
        System.out.println(Date.from(instant));
        System.out.println(instant.toEpochMilli());

        String t3 = "2020-12-01T06:00:39.3Z";
        instant = Instant.parse(t3);
        System.out.println(Date.from(instant));
        System.out.println(instant.toEpochMilli());

        String t4 = "2020-12-01T06:00:39Z";
        instant = Instant.parse(t4);
        System.out.println(Date.from(instant));
        System.out.println(instant.toEpochMilli());

    }
}
