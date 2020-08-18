package com.github.guang19.leaf.core.snowflake;

import com.github.guang19.leaf.core.IdGenerator;
import com.github.guang19.leaf.core.common.Result;
import com.github.guang19.leaf.core.snowflake.config.SnowflakeIdGeneratorProperties;
import com.github.guang19.leaf.core.snowflake.config.SnowflakeZookeeperHolderProperties;
import org.junit.Test;

import java.time.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author guang19
 * @date 2020/8/16
 * @description 雪花ID生成器测试
 */
public class SnowflakeIdGeneratorTest
{
    @Test
    public void test01() throws Exception
    {
        SnowflakeZookeeperHolderProperties zookeeperHolderProperties = new
                SnowflakeZookeeperHolderProperties(2181,"userservice","127.0.0.1","/home/guang19/tmp");

        SnowflakeIdGeneratorProperties idGeneratorProperties = new SnowflakeIdGeneratorProperties(1577811660000L,zookeeperHolderProperties);

        IdGenerator idGenerator = new SnowflakeIdGenerator(idGeneratorProperties);

        long begin = System.currentTimeMillis();
        for (int i = 0 ; i < 4095; ++i)
        {
            Result result = idGenerator.nextId();
            System.out.println(result.getId());
        }
        System.out.println("spend : " + (System.currentTimeMillis() - begin));
    }

    @Test
    public void test02() throws Exception
    {
        System.out.println(LocalDateTime.of(2020,1,1,1,1).toInstant(ZoneOffset.of("+8")).toEpochMilli());

        //2730227261100785664
        //6679516701194715136
        //6811788475881226240
        //83157078750789632

        //9223372036854775807
        System.out.println(Long.MAX_VALUE);

        System.out.println(LocalDateTime.ofInstant(Instant.ofEpochMilli(1288834974657L),ZoneId.systemDefault()));

        //1597654127362 = 2020-08-17 16:48:47.362
        System.out.println(LocalDateTime.ofInstant(Instant.ofEpochMilli(1597654127362L),ZoneId.systemDefault()));
    }

    @Test
    public void test03() throws Exception
    {
        //9223372036854775807
        //140737488355327
        System.out.println(5 << 1);
        System.out.println(Long.MAX_VALUE);

        TimeUnit.NANOSECONDS.sleep(TimeUnit.SECONDS.toNanos(3));
//        System.out.println();
    }

    @Test
    public void test04() throws Exception
    {
    }
}
