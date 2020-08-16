package com.github.guang19.leaf.server.service;

import com.github.guang19.leaf.core.IdGenerator;
import com.github.guang19.leaf.core.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SnowflakeIdGeneratorService
{
    private IdGenerator snowflakeIdGenerator;

    public SnowflakeIdGeneratorService(@Autowired IdGenerator snowflakeIdGenerator)
    {
        this.snowflakeIdGenerator = snowflakeIdGenerator;
    }

    //获取ID
    public Result get()
    {
        return snowflakeIdGenerator.nextId();
    }
}
