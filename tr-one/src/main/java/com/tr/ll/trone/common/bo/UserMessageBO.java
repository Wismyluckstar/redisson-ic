/**
 * FileName: User
 * Author:   yangyu
 * Date:     2020/7/13 9:03 下午
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.tr.ll.trone.common.bo;

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
public class UserMessageBO implements Serializable {

    private Long userId;

    //发布时间
    private double posted;

    //messageId
    private Long messageId;

    public final static String USERID = "userId";

    //发布时间
    public final static String POSTED = "posted";

    //messageId
    public final static String MESSAGEID = "messageId";

}