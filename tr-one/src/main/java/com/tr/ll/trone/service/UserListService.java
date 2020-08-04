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
 * 〈用户列表服务实现〉
 *
 * @author yangyu
 * @create 2020/7/14
 * @since 1.0.0
 */
public interface UserListService {

    /**
     * 功能描述: <br>
     * 〈  创建用户列表  〉
     *
     * @param userId
     * @param listName
     * @return:void
     * @since: 1.0.0
     * @Author:yangyu
     * @Date: 2020/7/15 9:12 下午
     */
   public Long createUserList(Long userId,String listName);

}