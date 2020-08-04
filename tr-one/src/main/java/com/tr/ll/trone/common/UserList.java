/**
 * FileName: Message
 * Author:   yangyu
 * Date:     2020/7/13 10:38 下午
 * Description: Share
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.tr.ll.trone.common;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 〈一句话功能简述〉<br> 
 * 〈用户列表信息〉
 *
 * @author yangyu
 * @create 2020/7/13
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class UserList {
    //列表名称
    private String name;
    //发布时间
    private Date created;
    //消息id
    private Long id;
    //用户id
    private Long uId;
    //用户名称
    private String login;


}