import com.eproxy.ProxyConfigure;
import com.eproxy.exception.DefaultExceptionHandler;
import com.eproxy.exception.DefaultSwitchPolicy;
import com.eproxy.loadbalance.LoadBalanceStrategy;
import com.eproxy.zookeeper.DefaultZookeeperServerDataResolver;
import org.junit.Test;
import redisclient.RedisClient;
import redisclient.RedisProxy;

/**
 * @author 谢俊权
 * @create 2016/5/6 10:25
 */
public class JunitTest {

    @Test
    public void redisTest() throws InterruptedException {

        ProxyConfigure proxyConfigure = new ProxyConfigure.Builder()
                .telnetTimeoutMs(1000 * 2)
                .checkServerAvailableIntervalMs(1000 * 10)
                .loadBalanceStrategy(LoadBalanceStrategy.WRR)
                .exceptionHandler(new DefaultExceptionHandler())
                .switchPolicy(new DefaultSwitchPolicy(60, 2))
                .zookeeperServerDataResolver(new DefaultZookeeperServerDataResolver())
                .build();

        RedisProxy redisProxy = new RedisProxy("redis-proxy.xml", proxyConfigure);
        for (int i = 0; i < 100; i++) {
            Thread.sleep(10000);
            RedisClient client = redisProxy.getClient();
            client.setex("hello", 60, "world" + i);
        }
    }
}
