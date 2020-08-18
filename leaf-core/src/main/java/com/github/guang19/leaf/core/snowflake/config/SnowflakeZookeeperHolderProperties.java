package com.github.guang19.leaf.core.snowflake.config;

import lombok.*;

/**
 * @author guang19
 * @date 2020/8/16
 * @description ZookeeperHolder配置属性
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SnowflakeZookeeperHolderProperties
{
    //service port
    private Integer servicePort;

    //服务名，默认为 {spring.application.name}
    private String serviceName;

    //zookeeper address
    private String zkConnectionString;

    //本地节点缓存的目录
    private String localNodeCacheDir;
}
