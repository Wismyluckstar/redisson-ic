/**
 * FileName: UserMessageServiceImpl
 * Author:   yy
 * Date:     2020/7/14 12:00 下午
 * Description: 用户发消息实现
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.tr.ll.trone.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tr.ll.trone.common.User;
import com.tr.ll.trone.common.UserMessage;
import com.tr.ll.trone.component.UserMessageSyncManager;
import com.tr.ll.trone.constant.RedisPrefix;
import com.tr.ll.trone.service.UserMessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.*;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * 〈一句话功能简述〉<br> 
 * 〈用户发消息实现〉
 *
 * @author yy
 * @create 2020/7/14
 * @since 1.0.0
 */
@Service
@Slf4j
public class UserMessageServiceImpl implements UserMessageService {

    @Resource
    private RedissonClient redissonClient;

    /**
     * @Description: 创建朋友圈消息
     * @Author yy
     * @Date 10:54 下午 2020/7/13
     * @Param  * @param null
     * @return
     **/
    @Override
    public Long createMessage(Long userId, String messageContent) {
        String userIdKey = RedisPrefix.KEY_PREFIX + "user:" + userId;
        RBatch batch = redissonClient.createBatch();
        batch.getMap(userIdKey).getAsync(User.LOGIN);
        //自增id获取
        batch.getAtomicLong(RedisPrefix.KEY_PREFIX + "message:id:").addAndGetAsync(1);
        RFuture<Integer> integerRFuture = batch.getMap(userIdKey).sizeAsync();

        RFuture<Boolean> booleanRFuture = batch.getMap(userIdKey).containsKeyAsync(User.LOGIN);
        BatchResult<?> execute = batch.execute();
        List<?> responses = execute.getResponses();
        String login = (String)responses.get(0);
        Long messageId = (Long)responses.get(1);
        try {
            Boolean aBoolean = booleanRFuture.get();
            Integer integer = integerRFuture.get();
            System.out.println("");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Object o = redissonClient.getMap(userIdKey).get(User.LOGIN);
        if(StringUtils.isBlank(login)){
            throw new RuntimeException("用户不存在");
        }
        //用户发送的消息内容
        UserMessage userMessage = new UserMessage().setId(messageId).setLogin(login)
                .setMessage(messageContent).setUId(userId).setPosted(new Date());
        batch = redissonClient.createBatch();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            batch.getMap(RedisPrefix.KEY_PREFIX + "message:"+messageId).putAllAsync(objectMapper.
                    readValue(objectMapper.writeValueAsString(userMessage), Map.class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            log.info("序列化用户消息缓存信息失败");
        }
        //更新用户已发布信息的数量
        batch.getMap(userIdKey).addAndGetAsync(User.POSTS,1);
        batch.execute();
        return messageId;
    }

    /**
     * @param userId
     * @param timeLine
     * @param page
     * @param pageSzie
     * @return
     * @Description: 获取用户主页时间线   换句话说就是朋友圈列表
     * # 它们分别用于指定函数要获取哪条时间线、要获取多少页时间线、以及每页要有多少条状态消息。
     * @Author yy
     * @Date 12:27 下午 2020/7/14
     * @Param * @param null
     */
    @Override
    public List<UserMessage> findUserMessage(Long userId, String timeLine, Integer page, Integer pageSzie) {
        RScoredSortedSet<Long> scoredSortedSet = redissonClient.getScoredSortedSet(timeLine + userId);
        //计算出开始位置和结束位置
        Collection<Long> messageIds = scoredSortedSet.valueRange((page - 1) * pageSzie, page * pageSzie - 1);


        RBatch batch = redissonClient.createBatch();
        messageIds.stream().forEach(messageId ->{
            batch.getMap(RedisPrefix.KEY_PREFIX + "message:"+messageId).readAllMapAsync();
        });
        BatchResult<?> execute = batch.execute();
        List<?> responses = execute.getResponses();
        List<UserMessage> userMessages = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        responses.forEach(response ->{
            if(response!= null){
                try {
                    userMessages.add(objectMapper.readValue(objectMapper.writeValueAsString(response),UserMessage.class));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        });
        return userMessages;
    }

    /**
     * @param userId
     * @param messageContent
     * @return
     * @Description: 发布朋友圈消息
     * @Author yy
     * @Date 12:34 下午 2020/7/15
     * @Param * @param null
     */
    @Override
    public Long postMessage(Long userId, String messageContent) {
        Long messageId = this.createMessage(userId, messageContent);
        if(messageId == null){
            return null;
        }
        Object posted = redissonClient.getMap(RedisPrefix.KEY_PREFIX + "message:" + messageId).get("posted");
        if(posted == null){
            return null;
        }
        Double postedDouble = Double.valueOf(String.valueOf(posted));
        //将消息添加到个人时间线内
        redissonClient.getScoredSortedSet(RedisPrefix.KEY_PREFIX + "profile:" + userId).addAsync(postedDouble,messageId);
        //将消息id同步到关注用户的时间线内
        this.syndicateMessage(userId,postedDouble,messageId);

        return messageId;
    }

    /**
     * @param userId
     * @param posted
     * @param messageId
     * @return
     * @Description: 将当前发布的状态同步到粉丝说说上
     * @Author yy
     * @Date 12:44 下午 2020/7/15
     */
    @Override
    public void deleteFollowersMessage(Long userId, double posted, Long messageId) {

        Collection<ScoredEntry<Object>> scoredEntries = deleteFollowersMessageForRedis(userId, posted, messageId);
        if(scoredEntries.size() >= POSTS_PER_PASS){
            //异步线程池处理更新其他用户的时间线
            UserMessageSyncManager.SYNC_MESSAGE_POOL.execute(() -> {
                Collection<ScoredEntry<Object>> scoredEntries1 = null;
                int start = POSTS_PER_PASS;
                do {
                    scoredEntries1 = deleteFollowersMessageForRedis(userId, posted, messageId);
                    start += POSTS_PER_PASS;
                }while(!CollectionUtils.isEmpty(scoredEntries1) && scoredEntries1.size() >= POSTS_PER_PASS);
            });
        }
    }

    public final static int POSTS_PER_PASS = 1000;
    /**
     * @param userId
     * @param posted
     * @param messageId
     * @return
     * @Description: 将当前发布的状态同步到粉丝说说上
     * @Author yy
     * @Date 12:44 下午 2020/7/15
     */
    @Override
    public void syndicateMessage(Long userId, double posted, Long messageId) {

        Collection<ScoredEntry<Object>> scoredEntries = syndicateMessageForRedis(userId, posted, messageId);
        if(scoredEntries.size() >= POSTS_PER_PASS){
            //异步线程池处理更新其他用户的时间线
            UserMessageSyncManager.SYNC_MESSAGE_POOL.execute(() -> {
                Collection<ScoredEntry<Object>> scoredEntries1 = null;
                int start = POSTS_PER_PASS;
                do {
                    scoredEntries1 = syndicateMessageForRedis(userId, posted, messageId);
                    start += POSTS_PER_PASS;
                }while(!CollectionUtils.isEmpty(scoredEntries1) && scoredEntries1.size() >= POSTS_PER_PASS);
            });
        }
    }

    /**
     * @Description: 同步消息
     * @Author yy
     * @Date 1:14 下午 2020/7/17
     * @Param  * @param null
     * @return
     **/
    private Collection<ScoredEntry<Object>> syndicateMessageForRedis(Long userId, double posted, Long messageId) {
        Collection<ScoredEntry<Object>> scoredEntries = redissonClient.getScoredSortedSet("followers:" + userId).entryRange(0, POSTS_PER_PASS);
        RBatch batch = redissonClient.createBatch();
        for (ScoredEntry<Object> scoredEntry : scoredEntries) {
            Long followUserId = (Long) scoredEntry.getValue();
            batch.getScoredSortedSet(RedisPrefix.KEY_PREFIX + "home:"+followUserId).addAsync(posted,messageId);
            batch.getScoredSortedSet(RedisPrefix.KEY_PREFIX + "home:"+followUserId).removeRangeByRankAsync(0, -UserFollowServiceImpl.HOME_TIMELINE_SIZE-1);
        }
        batch.execute();
        return scoredEntries;
    }

    /**
     * @Description: 同步消息
     * @Author yy
     * @Date 1:14 下午 2020/7/17
     * @Param  * @param null
     * @return
     **/
    private Collection<ScoredEntry<Object>> deleteFollowersMessageForRedis(Long userId, double posted, Long messageId) {
        Collection<ScoredEntry<Object>> scoredEntries = redissonClient.getScoredSortedSet(RedisPrefix.KEY_PREFIX + "followers:" + userId).entryRange(0, POSTS_PER_PASS);
        RBatch batch = redissonClient.createBatch();
        for (ScoredEntry<Object> scoredEntry : scoredEntries) {
            Long followUserId = (Long) scoredEntry.getValue();
            batch.getScoredSortedSet(RedisPrefix.KEY_PREFIX + "home:"+followUserId).removeAsync(followUserId);
        }
        batch.execute();
        return scoredEntries;
    }

    /**
     * @param userId
     * @param messageId
     * @return
     * @Description: 删除说说
     * @Author yy
     * @Date 1:08 下午 2020/7/15
     * @Param * @param null
     */
    @Override
    public void deleteMessage(Long userId, Long messageId) {
        String messageKey = RedisPrefix.KEY_PREFIX + "message:" + userId;
        redissonClient.getScoredSortedSet(messageKey).remove(messageId);
        //对指定的状态消息进行加锁，防止两个程序同时删除同一条状态消息的情况出现。
        RLock lock = redissonClient.getLock(messageKey + "lock");
        boolean res = lock.tryLock();
        try{
            if(!res){
                return;
            }
            //判断此条消息是否属于当前指定用户
            Object userIdCache = redissonClient.getMap(messageKey).get("userId");
            if(userIdCache == null|| !((Long)userIdCache).equals(userId)){
                return;
            }
            RBatch batch = redissonClient.createBatch();
            batch.getMap(messageKey).deleteAsync();
            batch.getScoredSortedSet(RedisPrefix.KEY_PREFIX + "profile:"+userId).removeAsync(messageId);
            batch.getScoredSortedSet(RedisPrefix.KEY_PREFIX + "home:"+userId).removeAsync(messageId);
            batch.getMap(RedisPrefix.KEY_PREFIX + "user:"+userId).addAndGetAsync(User.POSTS,-1);
            batch.execute();
            //把消息从粉丝的时间线中删除 TODO

        }finally {
           if(res){
               lock.unlock();
           }
        }




    }
}