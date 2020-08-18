package com.github.guang19.leaf.core.snowflake;

import com.github.guang19.leaf.core.common.Result;
import com.github.guang19.leaf.core.snowflake.config.SnowflakeIdGeneratorProperties;
import com.github.guang19.leaf.core.utils.IpUtils;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;


/**
 * @author   meituan leaf , guang19
 * @date 2020/8/16
 * @description  SnowflakeIdGenerator
 */
@Slf4j
@Getter
public class SnowflakeIdGenerator extends AbstractSnowflakeIdGenerator
{

    //ID生成器的开始时间，如果要在多台机器上部署，请确保每台机器上的这个配置都一致
    private final long startTimestamp;

    //工作机器ID : 10位 = 2^10 = 1024 ， 最多可以部署1024个ID服务，即 0 - 1023
    private final long WORKERID_BIT = 10L;
    //最大能够分配的 WorkerId = 1023
    private final long MAX_WORKERID_BIT = ~(-1L << WORKERID_BIT);

    //序列号: 12位 = 2^12 = 4096 , 毫秒内最多可以生成4096个ID，即: 0 - 4095
    private final long SEQUENCE_BIT = 12L;
    //毫秒内序列号的最大值 : 4095
    private final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    //工作机器ID的偏移量：工作机器ID位于12位序列号左边，所以序列号就是它的偏移量
    private final long WORKERID_LEFT_SHIFT = SEQUENCE_BIT;

    //时间戳偏移量： 时间戳位于工作机器ID的左边，所以 (序列号 + 工作机器ID) = 时间戳偏移量
    private final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BIT + WORKERID_BIT;

    private final long MAX_MOVED_BACK_OFFSET = 3;

    //当前工作机器的ID
    private long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    /**
     * 构造函数
     * @param idGeneratorProperties  配置属性
     */
    public SnowflakeIdGenerator(SnowflakeIdGeneratorProperties idGeneratorProperties)
    {
        this.startTimestamp = idGeneratorProperties.getStartTimestamp();
        Preconditions.checkArgument(getCurrentTmp() >= startTimestamp, "start time must lte to current time !");
        //获取当前服务器的某个网卡的IP
        final String ip = IpUtils.getIp();
        SnowflakeZookeeperHolder holder = new SnowflakeZookeeperHolder(idGeneratorProperties.getSnowflakeZookeeperHolderProperties(),ip);
        boolean initFlag = holder.init();
        if (initFlag)
        {
            this.workerId = holder.getWorkerId();
            if (workerId >= 0 && workerId <= MAX_WORKERID_BIT)
            {
                if (log.isInfoEnabled())
                {
                    log.info("Zookeeper initialized successfully , current work id is : {}", workerId);
                }
            }
            else
            {
                throw new IllegalStateException(
                        String.format("Zookeeper initialized successfully , but work id %d is not available , because work id must gte %d and lte %d"
                                ,workerId,0,MAX_WORKERID_BIT));
            }
        }
        else
        {
            throw new IllegalStateException("Zookeeper initialized failed!");
        }
        if (log.isInfoEnabled())
        {
            log.info("SnowflakeIdGenerator was created!");
        }
    }

    @Override
    public synchronized Result nextId()
    {
        long curTmp = getCurrentTmp();
        //时钟回拨
        if (curTmp < lastTimestamp)
        {
            long offset = lastTimestamp - curTmp;
            //如果回拨小于等于3ms，尚且在可接受的范围之内，就等其一倍的时间
            if (offset <= MAX_MOVED_BACK_OFFSET)
            {
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(MAX_MOVED_BACK_OFFSET << 1));
                curTmp = getCurrentTmp();
                if (curTmp < lastTimestamp)
                {
                    return Result.systemClockGoBack();
                }
            }
            else
            {
                //回拨步长太大，直接返回错误结果
                return Result.systemClockGoBack();
            }
        }
        //相同时间内，就自增序列
        else if (lastTimestamp == curTmp)
        {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //当前毫秒内的序列已达到最大
            if (sequence == 0)
            {
                curTmp = tillNextMillis(lastTimestamp);
            }
        }
        else
        {
            //正常获取，重置序列
            sequence = 0L;
        }
        lastTimestamp = curTmp;
        long id = ((curTmp - startTimestamp) << TIMESTAMP_LEFT_SHIFT) | (workerId << WORKERID_LEFT_SHIFT) | sequence;
        return Result.ok(id);
    }

    //获取当前工作机器ID
    @Override
    public long getWorkerId()
    {
        return workerId;
    }

    //获取开始时间
    public long getStartTimestamp()
    {
        return this.startTimestamp;
    }
}
