/**
 * FileName: UserService
 * Author:   yy
 * Date:     2020/7/13 8:06 下午
 * Description: 用户服务
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.tr.ll.trone.service;

/**
 * 〈一句话功能简述〉<br> 
 * 〈用户服务〉
 *
 * @author yy
 * @create 2020/7/13
 * @since 1.0.0
 */
public interface UserService {

    /**
     * 
     * @Description:  用户注册
     * @Author yy
     * @Date 8:08 下午 2020/7/13
     * @return 
     **/
    Long registerUser(String userName,String login);


    /**
     *
     * @Description: 用户登录
     * @Author yy
     * @Date 8:09 下午 2020/7/13
     * @return
     **/
    Long loginUser(String loginUser);


}