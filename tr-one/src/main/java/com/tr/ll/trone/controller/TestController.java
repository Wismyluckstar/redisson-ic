/**
 * FileName: TestController
 * Author:   yangyu
 * Date:     2020/7/13 12:45 下午
 * Description: 测试
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.tr.ll.trone.controller;

import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * 〈一句话功能简述〉<br> 
 * 〈测试〉
 *
 * @author yangyu
 * @create 2020/7/13
 * @since 1.0.0
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    RedissonClient redissonClient;

    @RequestMapping(value = "lock", method = RequestMethod.GET)
    public @ResponseBody
    void lock(@RequestParam("key") String key) {
        RBucket<Object> test = redissonClient.getBucket("test");
        test.set(new Object());
        System.out.println("");
    }

}