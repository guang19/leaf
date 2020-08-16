package com.github.guang19.leaf.core.snowflake;

import com.github.guang19.leaf.core.IdGenerator;
import com.github.guang19.leaf.core.common.Result;

/**
 * @author guang19
 * @date 2020/8/16
 * @description SnowflakeIdGenerator Template
 */
public abstract class AbstractSnowflakeIdGenerator implements IdGenerator
{

    public AbstractSnowflakeIdGenerator(){}

    @Override
    public abstract Result nextId();

    //阻塞直到下一毫秒
    protected long tillNextMillis(long lastTimestamp)
    {
        long timestamp = getCurrentTmp();
        while (timestamp <= lastTimestamp)
        {
            timestamp = getCurrentTmp();
        }
        return timestamp;
    }

    //获取当前时间，
    protected long getCurrentTmp()
    {
        return System.currentTimeMillis();
    }

    //获取当前工作机器ID
    public abstract long getWorkerId();
}
