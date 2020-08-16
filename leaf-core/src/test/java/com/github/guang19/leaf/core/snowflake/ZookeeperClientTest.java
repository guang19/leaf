package com.github.guang19.leaf.core.snowflake;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * @author guang19
 * @date 2020/8/16
 * @description zookeeper客户端操作测试
 */
public class ZookeeperClientTest
{
    private CuratorFramework curator ;
    @Before
    public void before() throws Exception
    {
        RetryPolicy retryPolicy = new RetryUntilElapsed(1000,10);
        this.curator = CuratorFrameworkFactory.newClient("127.0.0.1:2181",10000,6000,retryPolicy);
        curator.start();

    }

    @Test
    public void test01() throws Exception
    {
//        System.out.println(curator.create().forPath("/test"));
//        curator.create().forPath("/test/test02","abc".getBytes(StandardCharsets.UTF_8));
//        curator.delete().deletingChildrenIfNeeded().forPath("/test");
//        curator.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath("/test");
//        curator.setData().forPath("/test","abc".getBytes());
        //2020-08-16T23:30:10.875447
    }

    @Test
    public void test02() throws Exception
    {
        Stat stat = curator.checkExists().forPath("/a");
        System.out.println(stat);
    }
}
