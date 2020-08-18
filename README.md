# leaf

> There are no two identical leaves in the world.
>
> 世界上没有两片完全相同的树叶。
>
> ​								— 莱布尼茨

Meituan Leaf项目地址： [Leaf](https://github.com/Meituan-Dianping/Leaf)


## 介绍

相信各位同学或多或少都了解一点关于唯一ID的生成策略，我这里就简单介绍一下吧:

- 自增ID: 适用于单机或少量集群的应用环境，如果数据库实例过多，那么ID将会重复。

- UUID : 虽然UUID可以在很大程度上保证ID的唯一性，但是数据库索引大多为B+树结构，最好要求主键索引的值是有序且容易排序的，否则将会影响查询和修改索引的效率。所以UUID并不适用。

- 雪花ID: 推特开源的一种分布式唯一ID解决方案，支持64位(2^63 - 1)(可以减少位数)的最大值。雪花ID由时间戳,工作机器ID，自增序列3部分组成。
雪花ID有一个缺点，即它生成ID需要依赖系统时钟的有序性，所以系统时钟不能回拨，否则会造成ID重复的问题，所以便有了美团开源的Leaf。

- Leaf:Leaf是美团开源的分布式ID解决方案。他提供了号段模式和雪花ID 这2种模式来 进行ID的生成。
且它解决了雪花ID系统时钟回拨问题，但需要依赖于Zookeeper强一致性的中间件

以上就是一部分的ID解决方案了，它们各有优缺点，看各位同学如何选择。 

本项目根据美团Leaf改造而来，但只提供Snowflake方案，其实也算是了解了Leaf是如何解决时钟回拨问题的。


## 使用

本项目提供了两种使用方法，一种是直接与SpringBoot整合，另一种是使用Docker作为一个单独的服务。


### leaf-spring-boot-starter

环境：

- Jdk8 +

依赖:
````xml
<dependency>
  <groupId>com.github.guang19</groupId>
  <artifactId>leaf-spring-boot-starter</artifactId>
  <version>1.0.2</version>
</dependency>
````

引入依赖后，配置如下属性：

|     属性                          | 必需  |       默认值                                                               |        描述                       | 
|    :---                          | :---  |       :---                                                                |      :---                        |
|spring.leaf.snowflake.enable      | true  |       无                                                                  |  是否使用leaf.snowflake，请确保此配置为true，否则leaf将无法启用     |
|spring.leaf.snowflake.service-port| true  | 如果没有配置，那么将使用serve.port的值，如果server.port也没有配置，将发生异常    |  服务端口，此属性会作为leaf持久化节点路径的组成部分    |
|spring.leaf.snowflake.service-name| true  | 如果没有配置，那么将使用spring.application.name的值，如果spring.application.name也没有配置，将发生异常    |  服务名，此属性会作为leaf持久化节点路径的组成部分      |
|spring.leaf.snowflake.zk-connection-string | true | 无                                                              |   zookeeper的地址，支持集群       |
|spring.leaf.snowflake.local-node-cache-dir | false | 默认为操作系统的临时目录                                          |       本地节点缓存目录            |
|spring.leaf.snowflake.start-timestamp      | false | 默认为项目启动的时间                                              |      leaf开始时间戳(毫秒ms)。注意：Snowflake最多只支持70年左右的范围，所以不要设置的太早 |                         

PS: **如果你需要部署多个相同服务的leaf-server，请确保这些实例的service-name一定相同。这与Leaf的SnowflakeZookeeperHolder
相关，有兴趣的同学可以看看源码，我这里说一下原因。**

**举个例子：假设现在有2个leaf-server实例为user-service微服务提供用户ID，如果这两个leaf-server
的service-name不相同，那么它们的workId都将为0，因为SnowflakeZookeeperHolder在初始化的时候
会以service-name为持久化节点的路径创建子节点，service-name不同，则代表着路径不同，新创建的子节点
属于不同的路径，其序列号无法自增，总是为0，所以这些leaf-server的workId总是相同且为0。
workId为都相同就代表着这些leaf-server生成的Id总是重复的，毫无意义。**

**如果service-name相同，则SnowflakeZookeeperHolder会创建新的相同路径的子节点，
新创建的子节点会自增序列号，所以service-name相同，
则代表着第一个leaf-server的workId为0，第二个leaf-server的workId为1，以此类推。**

创建Service并注入SnowflakeIdGenerator，即可使用
`````java
@Service
public class SnowflakeIdGeneratorService
{
    private IdGenerator snowflakeIdGenerator;
    
    //构造器注入 SnowflakeIdGenerator
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
`````


### leaf - docker

环境：

- Jdk8 +

- Docker

````shell script
# clone 到本地
git clone https://github.com/guang19/leaf.git
````

````shell script
cd leaf

#修改配置
vim leaf-server/application.properties
vim leaf-server/application-prod.properties

#执行docker脚本，构建leaf-server镜像
cd scripts/docker
./build.sh

#启动leaf-server
sudo docker-compose up -d
````

确认leaf-server启动成功后，就可以正常使用了。

````shell script
#获取Id
curl -X GET http://localhost:13140/api/leaf/snowflake_id

#反解析Id
curl -X POST http://localhost:13140/api/decode/snowflakeId?snowflakeId=xxxxxx
````

**PS: 因为一般宿主机使用的是CST时间，而Docker容器内的是GMT时间，所以宿主机和Docker容器
可能会有8个小时左右的时差，但这并不影响使用，只是希望各位同学注意一下这个问题。**