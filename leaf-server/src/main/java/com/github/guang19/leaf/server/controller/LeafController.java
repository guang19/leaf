package com.github.guang19.leaf.server.controller;

import com.github.guang19.leaf.core.common.Result;
import com.github.guang19.leaf.core.common.Status;
import com.github.guang19.leaf.server.exception.LeafServerException;
import com.github.guang19.leaf.server.service.SnowflakeIdGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LeafController
{

    private SnowflakeIdGeneratorService snowflakeIdGeneratorService;

    public LeafController(@Autowired SnowflakeIdGeneratorService snowflakeIdGeneratorService)
    {
        this.snowflakeIdGeneratorService = snowflakeIdGeneratorService;
    }

    @RequestMapping(value = "/api/leaf/snowflake_id")
    public String getSnowflakeId()
    {
        return get(snowflakeIdGeneratorService.get());
    }

    private String get(Result result)
    {
        if (result.getStatus().equals(Status.EXCEPTION))
        {
            throw new LeafServerException(result.toString());
        }
        return String.valueOf(result.getId());
    }
}
