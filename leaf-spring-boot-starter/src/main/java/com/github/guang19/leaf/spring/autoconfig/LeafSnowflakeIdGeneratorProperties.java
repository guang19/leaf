package com.github.guang19.leaf.spring.autoconfig;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalDateTime;

/**
 * @author guang19
 * @date 2020/8/16
 * @description leaf zookeeper属性
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "spring.leaf.snowflake",ignoreInvalidFields = true)
public class LeafSnowflakeIdGeneratorProperties
{
    //leaf 开始时间，默认为当前项目启动的时间
    private Long startTimestamp;

    //zookeeper address
    private String zkConnectionString;

    //zookeeper port
    private Integer zkPort;

    //服务名，默认为 {spring.application.name}
    private String serviceName;

    //本地节点缓存的目录
    private String localNodeCacheDir;
}
