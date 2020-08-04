/**
 * FileName: RedisPrefix
 * Author:   yangyu
 * Date:     2020/8/3 11:23 下午
 * Description: redis前缀
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.tr.ll.trone.constant;

/**
 * 〈一句话功能简述〉<br> 
 * 〈redis前缀〉
 *
 * @author yangyu
 * @create 2020/8/3
 * @since 1.0.0
 */
public class RedisPrefix {
    //hashtag支持   我本地部署的时候出现master节点 第二台节点(slot  5461-10922)不会出现两个getMap(key) 第二次为空的情况
    //https://github.com/redisson/redisson/issues/2955
    public final static String KEY_PREFIX = "{social_c}:";

}