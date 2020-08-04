/**
 * FileName: UserFollowServiceImpl
 * Author:   yangyu
 * Date:     2020/7/14 12:52 下午
 * Description: 用户关注操作实现类
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.tr.ll.trone.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tr.ll.trone.common.UserMessage;
import com.tr.ll.trone.common.bo.UserMessageBO;
import com.tr.ll.trone.constant.RedisPrefix;
import com.tr.ll.trone.service.UserFollowService;
import org.redisson.api.BatchResult;
import org.redisson.api.RBatch;
import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.redisson.config.SentinelServersConfig;
import org.redisson.connection.balancer.RoundRobinLoadBalancer;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 〈一句话功能简述〉<br> 
 * 〈用户关注操作实现类〉
 *
 * @author yangyu
 * @create 2020/7/14
 * @since 1.0.0
 */
@Service
public class UserFollowServiceImpl implements UserFollowService {

    @Resource
    private RedissonClient redissonClient;
    //时间线size,用户取关后删除其他用户的数据
    public final static Integer HOME_TIMELINE_SIZE = 1000;

    /**
     * @param userId      关注发起人
     * @param beFollowUId
     * @return
     * @Description: 关注用户操作
     * @Author yangyu
     * @Date 12:50 下午 2020/7/14
     * @Param beFollowUId 被关注人
     **/
    @Override
    public void followUser(Long userId, Long beFollowUId) throws ExecutionException, InterruptedException {
        //粉丝key
        String followersKey = RedisPrefix.KEY_PREFIX + "followers:"+beFollowUId;
        //正在关注的用户key
        String followingKey = RedisPrefix.KEY_PREFIX + "following:"+userId;
        Double score = redissonClient.getScoredSortedSet(followingKey).getScore(userId);
        if(score != null){
            //说明已经关注了
            return;
        }
        long l = System.currentTimeMillis();
        //从正在关注的有序集合和关注者有序集合集合里面移除双方的用户id
        RBatch batch = redissonClient.createBatch();
        //删除正在关注的用户数
        batch.getScoredSortedSet(followingKey).addAndGetRankAsync(l,userId);
        batch.getScoredSortedSet(followersKey).addAndGetRankAsync(l,beFollowUId);
        batch.getScoredSortedSet(RedisPrefix.KEY_PREFIX + "profile:" + beFollowUId).entryRangeAsync(0, HOME_TIMELINE_SIZE - 1);
        BatchResult<?> execute = batch.execute();
        List<?> responses = execute.getResponses();
        Collection<ScoredEntry<Object>> scoredEntries = (Collection<ScoredEntry<Object>>) responses.get(2);
        batch = redissonClient.createBatch();
        for (ScoredEntry<Object> scoredEntry : scoredEntries) {
            Double posted = scoredEntry.getScore();
            Long messgeId = (Long) scoredEntry.getValue();
            batch.getScoredSortedSet(RedisPrefix.KEY_PREFIX + "home:"+userId).addAsync(posted,messgeId);
        }
        //只保留时间线上最新的1000条数据
        batch.getScoredSortedSet(RedisPrefix.KEY_PREFIX + "home:"+userId).removeRangeByRankAsync(0,-HOME_TIMELINE_SIZE-1);
        batch.execute();
    }

    /**
     * @param userId      取关发起人
     * @param beFollowUId
     * @return
     * @Description: 取关用户操作
     * @Author yangyu
     * @Date 12:50 下午 2020/7/14
     * @Param beFollowUId 被取关人
     **/
    @Override
    public void unFollowUser(Long userId, Long beFollowUId) {
        //粉丝key
        String followersKey = RedisPrefix.KEY_PREFIX + "followers:"+beFollowUId;
        //正在关注的用户key
        String followingKey = RedisPrefix.KEY_PREFIX + "following:"+userId;
        Double score = redissonClient.getScoredSortedSet(followingKey).getScore(beFollowUId);
        if(score == null){
            return;
        }
        //从正在关注的有序集合和关注者有序集合集合里面移除双方的用户id
        RBatch batch = redissonClient.createBatch();
        //删除正在关注的用户数
        batch.getScoredSortedSet(followingKey).removeAsync(beFollowUId);
        batch.getScoredSortedSet(followersKey).removeAsync(userId);
        batch.getScoredSortedSet(RedisPrefix.KEY_PREFIX + "profile:"+beFollowUId).valueRangeAsync(0,HOME_TIMELINE_SIZE-1);
        BatchResult<?> execute = batch.execute();
        List<UserMessage> messageList = (List<UserMessage>) execute.getResponses().get(2);
        batch = redissonClient.createBatch();
        batch.getMap(RedisPrefix.KEY_PREFIX + "user:"+userId).addAndGetAsync("following",-1);
        batch.getMap(RedisPrefix.KEY_PREFIX + "user:"+beFollowUId).addAndGetAsync("followers",-1);
        if(!CollectionUtils.isEmpty(messageList)){
            for (UserMessage userMessage : messageList) {
                batch.getScoredSortedSet(RedisPrefix.KEY_PREFIX + "home:"+userId).removeAsync(userMessage.getId());
            }
        }
        batch.execute();
    }

    public static void main(String[] args) throws JsonProcessingException {
        String xx = "{\"clientName\":\"redisson-demo\",\"connectTimeout\":10000,\"database\":0,\"dnsMonitoringInterval\":5000,\"failedSlaveCheckInterval\":3,\"failedSlaveReconnectionInterval\":3000,\"idleConnectionTimeout\":10000,\"keepAlive\":false,\"masterConnectionMinimumIdleSize\":32,\"masterConnectionPoolSize\":64,\"password\":\"123456\",\"pingConnectionInterval\":0,\"pingTimeout\":1000,\"readMode\":\"SLAVE\",\"retryAttempts\":3,\"retryInterval\":1500,\"scanInterval\":1000,\"sentinelAddresses\":[\"redis://127.0.0.1:26379\",\"redis://127.0.0.1:26380\"],\"slaveConnectionMinimumIdleSize\":32,\"slaveConnectionPoolSize\":64,\"slaveSubscriptionConnectionMinimumIdleSize\":1,\"slaveSubscriptionConnectionPoolSize\":50,\"sslEnableEndpointIdentification\":true,\"sslProvider\":\"JDK\",\"subscriptionConnectionMinimumIdleSize\":1,\"subscriptionConnectionPoolSize\":50,\"subscriptionMode\":\"MASTER\",\"subscriptionsPerConnection\":5,\"tcpNoDelay\":false,\"timeout\":3000}";
        ObjectMapper objectMapper = new ObjectMapper();
        SentinelServersConfig sentinelServersConfig = objectMapper.readValue(xx, SentinelServersConfig.class);
        System.out.println(sentinelServersConfig);
        RoundRobinLoadBalancer roundRobinLoadBalancer = new RoundRobinLoadBalancer();
        System.out.println(objectMapper.writeValueAsString(roundRobinLoadBalancer));

//        SentinelServersConfig sentinelServersConfig1 = new SentinelServersConfig();
////        sentinelServersConfig1.setLoadBalancer(null);
//        System.out.println(objectMapper.writeValueAsString(sentinelServersConfig1));
    }
}