/**
 * FileName: UserMessageService
 * Author:   yangyu
 * Date:     2020/7/13 10:53 下午
 * Description: 用户朋友圈消息管理
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.tr.ll.trone.service;

import com.tr.ll.trone.common.UserMessage;

import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈用户朋友圈消息管理〉
 *
 * @author yangyu
 * @create 2020/7/13
 * @since 1.0.0
 */
public interface UserMessageService {

    /**
     *
     * @Description: 创建朋友圈消息
     * @Author yangyu
     * @Date 10:54 下午 2020/7/13
     * @Param  * @param null
     * @return
     **/
    Long createMessage(Long userId,String messageContent);

    /**
     * @Description: 获取用户主页时间线   换句话说就是朋友圈列表
     * @Author yangyu
     * @Date 12:27 下午 2020/7/14
     * @Param  * @param null
     * @return
     **/
    List<UserMessage> findUserMessage(Long userId,String timeLine,Integer page,Integer pageSzie);

    /**
     *
     * @Description: 发布朋友圈消息
     * @Author yangyu
     * @Date 12:34 下午 2020/7/15
     * @Param  * @param null
     * @return
     **/
    Long postMessage(Long userId,String messageContent);

    /**
     * @Description: 将当前发布的状态同步到粉丝说说上
     * @Author yangyu
     * @Date 12:44 下午 2020/7/15
     * @return
     **/
    void syndicateMessage(Long userId,double posted,Long messageId);

    /**
     * @Description: 删除说说
     * @Author yangyu
     * @Date 1:08 下午 2020/7/15
     * @Param  * @param null
     * @return
     **/
    void deleteMessage(Long userId,Long messageId);

    /**
     * @param userId
     * @param posted
     * @param messageId
     * @return
     * @Description: 将当前发布的状态同步到粉丝说说上
     * @Author yangyu
     * @Date 12:44 下午 2020/7/15
     */
    void deleteFollowersMessage(Long userId, double posted, Long messageId);


}