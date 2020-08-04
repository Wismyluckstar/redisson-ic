package com.tr.ll.trone;

import com.tr.ll.trone.common.User;
import com.tr.ll.trone.common.UserMessage;
import com.tr.ll.trone.constant.RedisPrefix;
import com.tr.ll.trone.service.UserFollowService;
import com.tr.ll.trone.service.UserMessageService;
import com.tr.ll.trone.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.SetUtils;
import org.junit.jupiter.api.Test;
import org.redisson.api.*;
import org.redisson.transaction.TransactionException;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@SpringBootTest
@Log4j2
class TrOneApplicationTests {
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private UserService userService;
    @Resource
    private UserFollowService userFollowService;
    @Resource
    private UserMessageService userMessageService;

    @Test
    void contextLoads() throws ExecutionException, InterruptedException {
        //创建用户
        log.info("------------------这有个社交网站-----------------------");
        log.info("------------------我去注册个账号，就叫luxi吧-----------------------");
        userService.registerUser("路西", "luxi");
        Long luxiId = userService.loginUser("luxi");
        log.info("------------------luxi注册，登录成功，userId={}-------------",luxiId);
        log.info("------------------今天天气好，发个说说吧-------------",luxiId);
        Long messgeId = userMessageService.postMessage(luxiId, "今天天气非常好，适合钓鱼");
        log.info("------------------说说发布成功,messgeId={}-------------",messgeId);
        log.info("------------------又来一个小伙子名叫jierui,注意，他要入驻本平台了-------------",messgeId);
        userService.registerUser("杰瑞","jierui");
        Long jieruiId = userService.loginUser("jierui");
        log.info("------------------jierui注册，登录成功，userId={}-------------",jieruiId);
        log.info("------------------luxi给我二维码了，我要关注一波-------------");
        userFollowService.followUser(jieruiId,luxiId);
        log.info("------------------关注成功-------------");
        log.info("------------------去看看她朋友圈@@-------------");
        List<UserMessage> userMessage = userMessageService.findUserMessage(jieruiId, RedisPrefix.KEY_PREFIX+"home:", 0, 30);
        if(CollectionUtils.isEmpty(userMessage)){
            log.info("------------------什么都没有-------------");
        }else{
            log.info("------------------以下是朋友圈-------------");
            userMessage.forEach(e -> log.info("---------------content:{},time:{}----------------",e.getMessage(),e.getPosted()));
        }
        log.info("------------------以下是集群的经常经常出错的用法，需要使用hashtag处理-------------");
        //Redisson事务操作
        RTransaction transaction = redissonClient.createTransaction(TransactionOptions.defaults());
        RMap<String, String> map = transaction.getMap("{solt}myMap");
        map.put("1", "2");
        String value = map.get("3");
        RSet<String> set = transaction.getSet("{solt}mySet");
        set.add(value);
        try {
            transaction.commit();
        } catch(TransactionException e) {
            e.printStackTrace();
            transaction.rollback();
        }
        //redisson脚本执行
        //redissonClient.getBucket("{solt}foo").set("bar");
        String ss = "redis.call('del',KEYS[1])\n" +
                "return '\"success\"'";
        String r = redissonClient.getScript().eval(RScript.Mode.READ_WRITE,
                ss, RScript.ReturnType.VALUE, Arrays.asList("{test}xxx"));
        // 通过预存的脚本进行同样的操作
        RScript s = redissonClient.getScript();
        // 首先将脚本保存到所有的Redis主节点
//        String res = s.scriptLoad(ss);
//        System.out.println(res);
        // 返回值 res == bb4d6cb0da716db1e3fa4cff7f998661a009bf33
        // 再通过SHA值调用脚本
        Future<Object> r1 = redissonClient.getScript().evalShaAsync(RScript.Mode.READ_ONLY,
                "282297a0228f48cd3fc6a55de6316f31422f5d17",
                RScript.ReturnType.VALUE, Collections.emptyList());
        //redisson scan方式操作
        RKeys keys = redissonClient.getKeys();
        Iterable<String> keysByPattern = keys.getKeysByPattern("users:");
        keysByPattern.forEach(e -> {System.out.print("user:"+e);});
    }

}
