/**
 * FileName: UserFollowService
 * Author:   yangyu
 * Date:     2020/7/14 12:49 下午
 * Description: 用户关注服务实现
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.tr.ll.trone.service;

import java.util.concurrent.ExecutionException;

/**
 * 〈一句话功能简述〉<br> 
 * 〈用户关注服务实现〉
 *
 * @author yangyu
 * @create 2020/7/14
 * @since 1.0.0
 */
public interface UserFollowService {

    /**
     *
     * @Description: 关注用户操作
     * @Author yangyu
     * @Date 12:50 下午 2020/7/14
     * @Param  beFollowUId 被关注人
     * @param userId 关注发起人
     * @return
     **/
    void followUser(Long userId,Long beFollowUId) throws ExecutionException, InterruptedException;

    /**
     *
     * @Description: 取关用户操作
     * @Author yangyu
     * @Date 12:50 下午 2020/7/14
     * @Param  beFollowUId 被取关人
     * @param userId 取关发起人
     * @return
     **/
    void unFollowUser(Long userId,Long beFollowUId);



}