/**
 * FileName: UserServiceImpl
 * Author:   yangyu
 * Date:     2020/7/13 9:08 下午
 * Description: 用户服务实现
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.tr.ll.trone.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tr.ll.trone.common.User;
import com.tr.ll.trone.constant.RedisPrefix;
import com.tr.ll.trone.service.UserService;
import org.redisson.api.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 〈一句话功能简述〉<br> 
 * 〈用户服务实现〉
 *
 * @author yangyu
 * @create 2020/7/13
 * @since 1.0.0
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private RedissonClient redissonClient;

    @Override
    public Long registerUser(String userName, String login) {
        //取小写
        String loginLower = login.toLowerCase();
        RLock lock = redissonClient.getLock("user:" + loginLower+":lock");
        boolean res = false;
        try {
            res = lock.tryLock();
            if(!res){
               return null;
            }
            RMap<String, Object> userInfo = redissonClient.getMap(RedisPrefix.KEY_PREFIX + "users:");
            Object id = userInfo.get(loginLower);
            //用户存在的时候
            if(id != null){
                return (Long)id;
            }
            //生成递增id
            RAtomicLong userIncr = redissonClient.getAtomicLong(RedisPrefix.KEY_PREFIX + "user:id:");
            long userId = userIncr.addAndGet(1);
            //生成用户
            User user = new User().setFollowers(0).setFollowing(0).setId(userId).setLogin(login).setName(userName).setPosts(0).setRegisterTime(new Date());
            ObjectMapper objectMapper = new ObjectMapper();
            RBatch batch = redissonClient.createBatch();
            //放入用户数据
            batch.getMap(RedisPrefix.KEY_PREFIX + "user:"+userId).putAllAsync(objectMapper.readValue(objectMapper.writeValueAsString(user),Map.class));
            //将用户名字和对应的id存入hash中
            batch.getMap(RedisPrefix.KEY_PREFIX + "users:").putAsync(loginLower,userId);
            batch.getMap(RedisPrefix.KEY_PREFIX + "users:").getAsync(loginLower);
            BatchResult<?> execute = batch.execute();
            return userId;
        }catch (Exception e){
            throw new RuntimeException("用户名称已经存在");
        }finally {
            if(res){
                lock.unlock();
            }
        }
    }

    @Override
    public Long loginUser(String loginLower) {

        RMap<String, Object> map = redissonClient.getMap(RedisPrefix.KEY_PREFIX + "users:");
        Object o = map.get(loginLower);
        if(o == null){
            return this.registerUser("游客",loginLower);
        }
        return (Long)o;
    }
}