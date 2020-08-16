package com.github.guang19.leaf.spring.autoconfig;

import com.github.guang19.leaf.core.snowflake.SnowflakeIdGenerator;
import com.github.guang19.leaf.core.snowflake.config.SnowflakeIdGeneratorProperties;
import com.github.guang19.leaf.core.snowflake.config.SnowflakeZookeeperHolderProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author guang19
 * @date 2020/8/16
 * @description Leaf自动配置
 */
@SpringBootConfiguration
@ConditionalOnClass(SnowflakeIdGenerator.class)
@EnableConfigurationProperties(LeafSnowflakeIdGeneratorProperties.class)
public class LeafAutoConfiguration
{

    @Bean
    @ConditionalOnProperty(prefix = "spring.leaf.snowflake" ,value = "enable" , havingValue = "true")
    public SnowflakeIdGenerator snowflakeIdGenerator(@Autowired LeafSnowflakeIdGeneratorProperties leafSnowflakeIdGeneratorProperties,
                                                     @Autowired Environment env)
    {
        SnowflakeIdGeneratorProperties snowflakeIdGeneratorProperties = new SnowflakeIdGeneratorProperties();
        if (leafSnowflakeIdGeneratorProperties.getStartTimestamp() == null)
        {
            snowflakeIdGeneratorProperties.setStartTime(LocalDateTime.now(Clock.systemDefaultZone()));
        }
        else
        {
            snowflakeIdGeneratorProperties.setStartTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(leafSnowflakeIdGeneratorProperties.getStartTimestamp()), ZoneId.systemDefault()));
        }
        SnowflakeZookeeperHolderProperties snowflakeZookeeperHolderProperties = new SnowflakeZookeeperHolderProperties();
        if (leafSnowflakeIdGeneratorProperties.getZkConnectionString() == null)
        {
            throw new RuntimeException("The property of snowflakeIdGenerator [connection string] must be configured");
        }
        snowflakeZookeeperHolderProperties.setZkConnectionString(leafSnowflakeIdGeneratorProperties.getZkConnectionString());
        if (leafSnowflakeIdGeneratorProperties.getZkPort() == null)
        {
            throw new RuntimeException("The property of snowflakeIdGenerator [port] must be configured");
        }
        snowflakeZookeeperHolderProperties.setZkPort(leafSnowflakeIdGeneratorProperties.getZkPort());
        if (leafSnowflakeIdGeneratorProperties.getLocalNodeCacheDir() == null)
        {
            snowflakeZookeeperHolderProperties.setLocalNodeCacheDir(System.getProperty("java.io.tmpdir"));
        }
        else
        {
            snowflakeZookeeperHolderProperties.setLocalNodeCacheDir(leafSnowflakeIdGeneratorProperties.getLocalNodeCacheDir());
        }
        if (leafSnowflakeIdGeneratorProperties.getServiceName() != null)
        {
            snowflakeZookeeperHolderProperties.setServiceName(leafSnowflakeIdGeneratorProperties.getServiceName());
        }
        else if (env.getProperty("spring.application.name") != null)
        {
            snowflakeZookeeperHolderProperties.setServiceName(env.getProperty("spring.application.name"));
        }
        else
        {
            throw new RuntimeException("The property of snowflakeIdGenerator [service name] must be configured");
        }
        snowflakeIdGeneratorProperties.setSnowflakeZookeeperHolderProperties(snowflakeZookeeperHolderProperties);
        return new SnowflakeIdGenerator(snowflakeIdGeneratorProperties);
    }
}
