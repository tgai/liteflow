/**
 * <p>Title: liteflow</p>
 * <p>Description: 轻量级的组件式流程框架</p>
 *
 * @author Bryan.Zhang
 * @email weenyc31@163.com
 * @Date 2020/4/1
 */
package com.yomahub.liteflow.thread;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.yomahub.liteflow.exception.ThreadExecutorServiceCreateException;
import com.yomahub.liteflow.property.LiteflowConfig;
import com.yomahub.liteflow.property.LiteflowConfigGetter;
import com.yomahub.liteflow.spi.holder.ContextAwareHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;


/**
 * 线程池工具类
 *
 * @author Bryan.Zhang
 */
public class ExecutorHelper {

    private final Logger LOG = LoggerFactory.getLogger(ExecutorHelper.class);

    /**
     * 此处使用Map缓存线程池信息
     * key - 线程池构建者的Class全类名
     * value - 线程池对象
     */
    private final Map<String, ExecutorService> executorServiceMap;

    private ExecutorHelper() {
        executorServiceMap = Maps.newConcurrentMap();
    }

    /**
     * 使用静态内部类实现单例模式
     */
    private static class Holder {
        static final ExecutorHelper INSTANCE = new ExecutorHelper();
    }

    public static ExecutorHelper loadInstance() {
        return Holder.INSTANCE;
    }

    /**
     *
     * <p>
     *
     * @param pool 需要关闭的线程组.
     */
    public void shutdownAwaitTermination(ExecutorService pool) {
        shutdownAwaitTermination(pool, 60L);
    }

    /**
     * <p>
     * 关闭ExecutorService的线程管理者
     * <p>
     *
     * @param pool    需要关闭的管理者
     * @param timeout 等待时间
     */
    public void shutdownAwaitTermination(ExecutorService pool,
                                         long timeout) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                    LOG.error("Pool did not terminate.");
                }
            }
        } catch (InterruptedException ie) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    //构建全局默认线程池
    public ExecutorService buildExecutor() {
        LiteflowConfig liteflowConfig = LiteflowConfigGetter.get();
        if (!executorServiceMap.containsKey(liteflowConfig.getThreadExecutorClass())) {
            ExecutorService executorService = getExecutorBuilder(liteflowConfig.getThreadExecutorClass()).buildExecutor();
            executorServiceMap.put(liteflowConfig.getThreadExecutorClass(), executorService);
        }
        return executorServiceMap.get(liteflowConfig.getThreadExecutorClass());
    }

    //构建线程池执行器 - 支持多个when公用一个线程池
    public ExecutorService buildExecutor(String threadExecutorClass) {
        if (StrUtil.isBlank(threadExecutorClass)) {
            return buildExecutor();
        }
        ExecutorService executorServiceFromCache = executorServiceMap.get(threadExecutorClass);
        if (executorServiceFromCache != null) {
            return executorServiceFromCache;
        } else {
            ExecutorService executorService = getExecutorBuilder(threadExecutorClass).buildExecutor();
            executorServiceMap.put(threadExecutorClass, executorService);
            return executorService;
        }
    }

    /**
     * <p>
     * 根据线程执行构建者Class类名获取ExecutorBuilder实例
     * </p>
     *
     * @param threadExecutorClass 线程执行class全量名
     * @return com.yomahub.liteflow.thread.ExecutorBuilder
     * @author sikadai
     * @date 2022/1/21 23:04
     */
    private ExecutorBuilder getExecutorBuilder(String threadExecutorClass) {
        try {
            Class<ExecutorBuilder> executorClass  = (Class<ExecutorBuilder>) Class.forName(threadExecutorClass);
            return ContextAwareHolder.loadContextAware().registerBean(executorClass);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ThreadExecutorServiceCreateException(e.getMessage());
        }
    }

    public void clearExecutorServiceMap(){
        if (MapUtil.isNotEmpty(executorServiceMap)){
            executorServiceMap.clear();
        }
    }
}
