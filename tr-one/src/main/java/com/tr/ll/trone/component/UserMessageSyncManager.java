/**
 * FileName: UserMessageSyncManager
 * Author:   yy
 * Date:     2020/7/17 12:56 下午
 * Description: 用户消息同步管理
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.tr.ll.trone.component;

import jodd.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * 〈一句话功能简述〉<br> 
 * 〈用户消息同步管理〉
 *
 * @author yy
 * @create 2020/7/17
 * @since 1.0.0
 */
public class UserMessageSyncManager {
    private final static ThreadFactory NAMED_THREAD_FACTORY = new ThreadFactoryBuilder().setNameFormat("sync-message-%d").get();

    public final static ExecutorService SYNC_MESSAGE_POOL = new ThreadPoolExecutor(4, 10, 0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<Runnable>(1024),
            NAMED_THREAD_FACTORY,
            new ThreadPoolExecutor.AbortPolicy());

}