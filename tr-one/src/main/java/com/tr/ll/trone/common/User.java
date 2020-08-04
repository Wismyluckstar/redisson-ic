/**
 * FileName: User
 * Author:   yangyu
 * Date:     2020/7/13 9:03 下午
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.tr.ll.trone.common;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 〈一句话功能简述〉<br> 
 * 〈用户信息〉
 *
 * @author yangyu
 * @create 2020/7/13
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class User implements Serializable {

    private Long id;

    //昵称
    private String login;

    //用户姓名
    private String name;

    //分数数量
    private Integer followers;

    //正在关注用户
    private Integer following;

    //已发布状态消息的数量
    private Integer posts;

    //注册时间
    private Date registerTime;


    public final static String ID = "id";

    //昵称
    public final static String LOGIN = "login";

    //用户姓名
    public final static String NAME = "name";

    //分数数量
    public final static String FOLLOWER = "followers";

    //正在关注用户
    public final static String FOLLOWING = "following";

    //已发布状态消息的数量
    public final static String POSTS = "posts";

    //注册时间
    public final static String REGISTERTIME = "registerTime";

}