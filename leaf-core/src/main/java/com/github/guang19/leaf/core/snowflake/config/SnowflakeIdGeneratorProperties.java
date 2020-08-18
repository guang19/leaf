package com.github.guang19.leaf.core.snowflake.config;

import lombok.*;


/**
 * @author guang19
 * @date 2020/8/16
 * @description SnowflakeIdGenerator配置属性
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SnowflakeIdGeneratorProperties
{
    //leaf 开始时间，默认为当前项目启动的时间
    private Long startTimestamp;

    private SnowflakeZookeeperHolderProperties snowflakeZookeeperHolderProperties;
}
