package com.github.guang19.leaf.server.controller;

import com.github.guang19.leaf.core.snowflake.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author  meituan leaf  ,guang19
 * @date 2020/8/16
 * @description  反解析 Snowflake Id的信息
 */
@RestController
public class LeafMonitorController
{

    private SnowflakeIdGenerator snowflakeIdGenerator;

    public LeafMonitorController(@Autowired SnowflakeIdGenerator snowflakeIdGenerator)
    {
        this.snowflakeIdGenerator = snowflakeIdGenerator;
    }

    /**
     * the output is like this:
     * {
     *   "timestamp": "1567733700834(2019-09-06 09:35:00.834)",
     *   "sequenceId": "3448",
     *   "workerId": "39"
     * }
     */
    @RequestMapping(value = "/api/decode/snowflakeId")
    public Map<String, String> decodeSnowflakeId(@RequestParam("snowflakeId") String snowflakeIdStr)
    {
        Map<String, String> map = new HashMap<>();
        try
        {
            long snowflakeId = Long.parseLong(snowflakeIdStr);
            //右移22位就是时间戳的位置
            long originTimestamp = (snowflakeId >> 22) + snowflakeIdGenerator.getStartTimestamp();
            LocalDateTime originDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(originTimestamp), ZoneId.systemDefault());
            map.put("timestamp", "originTimestamp(" + originDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")) +")");

            //右移12位正好是工作机器的ID
            long workerId = (snowflakeId >> 12) ^ (snowflakeId >> 22 << 10);
            map.put("workerId", String.valueOf(workerId));

            //无需移动就是自增序列
            long sequence = snowflakeId ^ (snowflakeId >> 12 << 12);
            map.put("sequenceId", String.valueOf(sequence));
        }
        catch (NumberFormatException e)
        {
            map.put("errorMsg", "snowflake Id反解析发生异常!");
        }
        return map;
    }
}
