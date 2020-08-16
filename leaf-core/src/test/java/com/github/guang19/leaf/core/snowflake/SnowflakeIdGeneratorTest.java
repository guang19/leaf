package com.github.guang19.leaf.core.snowflake;

import com.github.guang19.leaf.core.IdGenerator;
import com.github.guang19.leaf.core.common.Result;
import com.github.guang19.leaf.core.snowflake.config.SnowflakeIdGeneratorProperties;
import com.github.guang19.leaf.core.snowflake.config.SnowflakeZookeeperHolderProperties;
import org.junit.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

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
                SnowflakeZookeeperHolderProperties("127.0.0.1",2181,"userservice","/home/guang19/tmp");

        SnowflakeIdGeneratorProperties idGeneratorProperties = new SnowflakeIdGeneratorProperties(
                LocalDateTime.of(2000,1,1,1,1),zookeeperHolderProperties);

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
        System.out.println(LocalDateTime.of(1969,3,1,1,1).toInstant(ZoneOffset.of("+8")).toEpochMilli());

        //2730227261100785664
        //6679516701194715136
        //6811788475881226240

        //9223372036854775807
        System.out.println(Long.MAX_VALUE);

    }
}
