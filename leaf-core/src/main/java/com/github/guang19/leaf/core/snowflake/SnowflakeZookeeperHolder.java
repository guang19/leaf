package com.github.guang19.leaf.core.snowflake;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.guang19.leaf.core.snowflake.config.SnowflakeZookeeperHolderProperties;
import com.github.guang19.leaf.core.snowflake.exception.CheckLastTimeException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.apache.curator.framework.CuratorFrameworkFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author guang19  , meituan leaf
 * @date 2020/8/16
 * @description  SnowflakeZookeeperHolder
 */

@Slf4j
@Getter
public class SnowflakeZookeeperHolder
{
    //节点前缀
    private final String PREFIX_ZK_PATH;

    //持久数据的节点的路径
    private final String PATH_FOREVER;

    //本地缓存的workId文件
    private final String PROP_PATH;

    //leaf zk持久化节点 : PATH_FOREVER/ip:port-0000000000
    private String zk_AddressNode;

    //ip + port
    private String listenAddress;

    //workId
    private int workerId;

    //服务器某个网卡的ip
    private String ip;
    //zk port
    private int port;
    //zk address
    private String connectionString;
    //最后一次上传数据的时间
    private long lastUpdateTime;

    //zk 客户端
    private final CuratorFramework curator;

    /**
     * 构造函数
     * @param zookeeperHolderProperties  配置属性
     * @param ip  当前服务器的某个网卡的ip
     */
    public SnowflakeZookeeperHolder(SnowflakeZookeeperHolderProperties zookeeperHolderProperties,String ip)
    {
        this.connectionString = zookeeperHolderProperties.getZkConnectionString();
        this.port = zookeeperHolderProperties.getZkPort();
        this.ip = ip;
        this.listenAddress = ip + ":" + port;
        this.curator = curatorFramework(connectionString);
        this.PREFIX_ZK_PATH = "/leaf/snowflake/" + zookeeperHolderProperties.getServiceName();
        this.PATH_FOREVER = PREFIX_ZK_PATH + "/forever";
        this.PROP_PATH = zookeeperHolderProperties.getLocalNodeCacheDir() +  File.separator + zookeeperHolderProperties.getServiceName()
                + "/leafconf/%d/workerId.properties";
    }

    //初始化 节点
    public boolean init()
    {
        try
        {
            curator.start();
            Stat stat = curator.checkExists().forPath(PATH_FOREVER);
            //不存在根节点,机器第一次启动
            if (stat == null)
            {
                //创建节点,并上传数据
                zk_AddressNode = createNode();
                //worker id 默认是0
                updateLocalWorkerId(workerId);
                //定时上报本机时间给forever节点
                scheduledUploadData(zk_AddressNode);
                return true;
            }
            //存在根节点，说明worker机器是重新启动的,先检查是否有属于自己的根节点
            else
            {
                // {ip:port = 00001}
                Map<String, Integer> nodeMap = new HashMap<>();
                //{ip:port->(ip:port-000001)}
                Map<String, String> realNode = new HashMap<>();
                List<String> keys = curator.getChildren().forPath(PATH_FOREVER);
                for (String key : keys)
                {
                    String[] nodeKey = key.split("-");
                    realNode.put(nodeKey[0], key);
                    nodeMap.put(nodeKey[0], Integer.parseInt(nodeKey[1]));
                }
                Integer _workerId = nodeMap.get(listenAddress);
                //使用的仍然是原来的节点
                if (_workerId != null)
                {
                    //有自己的节点,zk_AddressNode=ip:port
                    zk_AddressNode = PATH_FOREVER + "/" + realNode.get(listenAddress);
                    //启动worker时使用会使用
                    workerId = _workerId;
                    if (!checkInitTimeStamp(zk_AddressNode))
                    {
                        throw new CheckLastTimeException("Init timestamp check error , forever node timestamp gt current server time !");
                    }
                    //准备创建临时节点
                    scheduledUploadData(zk_AddressNode);
                    updateLocalWorkerId(_workerId);
                    if (log.isInfoEnabled())
                    {
                        log.info("[Old NODE]find forever node have this endpoint ip-{} port-{} worker id-{} child node and start SUCCESS !", ip, port, _workerId);
                    }
                }
                else
                {
                    //创建新的持久节点 ,不用check时间
                    String newNode = createNode();
                    zk_AddressNode = newNode;
                    String[] nodeKey = newNode.split("-");
                    workerId = Integer.parseInt(nodeKey[1]);
                    scheduledUploadData(zk_AddressNode);
                    updateLocalWorkerId(workerId);
                    if (log.isInfoEnabled())
                    {
                        log.info("[New NODE]can not find node on forever node that endpoint ip-{} port-{} worker id-{},create own node on forever node and start SUCCESS !", ip, port, workerId);
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error("Capture an exception while initializing zookeeper node ",e);
            try
            {
                //初始化zk节点失败后，使用本地缓存的节点
                Properties properties = new Properties();
                properties.load(new FileInputStream(new File(String.format(PROP_PATH,port))));
                workerId = Integer.parseInt(properties.getProperty("workerId"));
                log.warn("SnowflakeIdGenerator Zookeeper node initialization failed , use local cache node workerId - {}", workerId);
            }
            catch (IOException ioe)
            {
                log.error("Capture an exception while reading local cache file " , ioe);
                return false;
            }
        }
        return true;
    }

    //定时向zk更新数据
    private void scheduledUploadData(final String zk_AddressNode)
    {
        //每3s上报数据
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1,
                r ->
                {
                    Thread thread = new Thread(r, "snowflake-idgenerator-schedule-upload-time");
                    thread.setDaemon(true);
                    return thread;
                }, new ThreadPoolExecutor.CallerRunsPolicy());
        scheduledThreadPoolExecutor.setMaximumPoolSize(1);
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(() -> updateNewData(zk_AddressNode), 1L, 3L, TimeUnit.SECONDS);
    }

    //更新节点数据
    private void updateNewData(String path)
    {
        try
        {
            if (System.currentTimeMillis() >= lastUpdateTime)
            {
                curator.setData().forPath(path, buildData().getBytes(StandardCharsets.UTF_8));
                lastUpdateTime = System.currentTimeMillis();
            }
        }
        catch (Exception e)
        {
            log.error("Capture an exception while update node data . node path is {} , error is {}", path, e);
        }
    }

    /**
     * 在节点文件系统上缓存一个work id，当zk失效时，机器重启时保证能够正常启动
     */
    private void updateLocalWorkerId(int workerId)
    {
        File leafConfFile = new File(String.format(PROP_PATH,port));
        //如果缓存文件已存在，则直接缓存
        if (leafConfFile.exists())
        {
            try(OutputStream outputStream = openFileOutputStream(leafConfFile))
            {
                outputStream.write(("workerID=" + workerId).getBytes(StandardCharsets.UTF_8));
                if (log.isInfoEnabled())
                {
                    log.info("Update local file cache workerId is : {}", workerId);
                }
            }
            catch (IOException e)
            {
                log.error("Capture an exception while updating local file cache  ", e);
            }
        }
        else
        {
            //如果文件不存在，先判断其父目录是否存在，再创建文件并缓存
            File parentFile = leafConfFile.getParentFile();
            boolean flag = parentFile.exists();
            if (!flag)
            {
                flag = parentFile.mkdirs();
            }
            if (flag)
            {
                try(OutputStream outputStream = openFileOutputStream(leafConfFile))
                {
                    outputStream.write(("workerID=" + workerId).getBytes(StandardCharsets.UTF_8));
                    if (log.isInfoEnabled())
                    {
                        log.info("Create local file cache workerId is : {}", workerId);
                    }
                }
                catch (IOException e)
                {
                    log.error("Capture an exception while updating local cache file ", e);
                }
            }
            else
            {
                log.error("An error occurred while creating local cache file parent dir !");
            }
        }
    }

    //初始化时，如果节点已存在，说明是重新启动，就需要检查节点的时间戳
    private boolean checkInitTimeStamp(String zk_AddressNode) throws Exception
    {
        byte[] bytes = curator.getData().forPath(zk_AddressNode);
        Endpoint endPoint = parseBuildData(new String(bytes));
        //服务器的时间不能小于最后一次上报的时间
        return endPoint.getTimestamp() < System.currentTimeMillis();
    }


    /**
     * 创建持久顺序节点 ,并把节点数据放入 value
     * @return                  创建后的节点
     * @throws Exception        exception
     */
    private String createNode() throws Exception
    {
        try
        {
            return curator.create().creatingParentsIfNeeded().
                    withMode(CreateMode.PERSISTENT_SEQUENTIAL).
                    forPath(PATH_FOREVER + "/" + listenAddress + "-", buildData().getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e)
        {
            log.error("Capture exception while creating zookeeper node ", e);
            throw e;
        }
    }

    /**
     * 构建需要上传的数据
     * @return json data of Endpoint
     */
    private String buildData() throws JsonProcessingException
    {
        Endpoint endpoint = new Endpoint(ip, port, System.currentTimeMillis());
        return new ObjectMapper().writeValueAsString(endpoint);
    }

    /**
     * 解析节点的数据为Endpoint
     * @param json           节点数据
     * @return               Endpoint
     * @throws IOException   IOException
     */
    private Endpoint parseBuildData(String json) throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, Endpoint.class);
    }

    //创建zk客户端
    private CuratorFramework curatorFramework(String connectionString)
    {
        return CuratorFrameworkFactory.builder().connectString(connectionString)
                .retryPolicy(new RetryUntilElapsed(1000, 4))
                .connectionTimeoutMs(10000)
                .sessionTimeoutMs(6000)
                .build();
    }

    //创建指定文件的输入流
    private FileOutputStream openFileOutputStream(File file) throws FileNotFoundException
    {
        return new FileOutputStream(file,false);
    }

    /**
     * 上报数据结构
     */
    @Setter
    @Getter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    static class Endpoint
    {
        private String ip;
        private int port;
        private long timestamp;
    }

}
