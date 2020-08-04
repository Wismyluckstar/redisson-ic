/**
 * FileName: UserFollowService
 * Author:   yy
 * Date:     2020/7/14 12:49 下午
 * Description: 用户关注服务实现
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.tr.ll.trone.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tr.ll.trone.common.User;
import com.tr.ll.trone.common.UserList;
import com.tr.ll.trone.constant.RedisPrefix;
import com.tr.ll.trone.service.UserListService;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.BatchResult;
import org.redisson.api.RBatch;
import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 〈一句话功能简述〉<br> 
 * 〈用户列表服务实现〉
 *
 * @author yy
 * @create 2020/7/14
 * @since 1.0.0
 */
@Service
@Log4j2
public class UserListServiceImpl implements UserListService {

    @Resource
    private RedissonClient redissonClient;


    /**
     * 功能描述: <br>
     * 〈  创建用户列表  〉
     *
     * @param userId
     * @param listName
     * @return:void
     * @since: 1.0.0
     * @Author:yy
     * @Date: 2020/7/15 9:12 下午
     */
    @Override
    public Long createUserList(Long userId, String listName) {
        RBatch batch = redissonClient.createBatch();
        batch.getMap(RedisPrefix.KEY_PREFIX + "user:" + userId).getAsync(User.LOGIN);
        batch.getAtomicLong(RedisPrefix.KEY_PREFIX + "list:id:").addAndGetAsync(1);
        BatchResult<?> execute = batch.execute();
        List<?> responses = execute.getResponses();
        Object login = responses.get(0);
        RFuture<Long> longRFuture = (RFuture<Long>) responses.get(1);
        if(login == null){
            //用户不存在则直接返回
            return null;
        }
        Long listId = null;
        try {
            listId = longRFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if(listId == null){
            log.info("生成列表id失败");
            return null;
        }
        batch = redissonClient.createBatch();
        batch.getScoredSortedSet(RedisPrefix.KEY_PREFIX + "lists:"+userId).addAndGetRankAsync(System.currentTimeMillis(),listId);
        UserList userList = new UserList().setId(listId).setLogin(String.valueOf(login)).setName(listName).setUId(userId).setCreated(new Date());
        ObjectMapper objectMapper = new ObjectMapper();
        String s = null;
        try {
            batch.getMap(RedisPrefix.KEY_PREFIX + "list:"+userId).putAllAsync(
                    objectMapper.readValue(objectMapper.writeValueAsString(userList), Map.class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            log.info("序列化用户列表数据失败");
            return null;
        }
        batch.execute();
        return listId;
    }
}