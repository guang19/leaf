package com.github.guang19.leaf.core.snowflake.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author guang19
 * @date 2020/8/16
 * @description ZookeeperHolder配置属性
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SnowflakeZookeeperHolderProperties
{
    //zookeeper address
    private String zkConnectionString;

    //zookeeper port
    private Integer zkPort;

    //服务名，默认为 {spring.application.name}
    private String serviceName;

    //本地节点缓存的目录
    private String localNodeCacheDir;
}
