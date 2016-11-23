package com.eproxy.exception;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 谢俊权
 * @create 2016/8/31 17:57
 */
public class ExceptionPolicy {

    private long minExceptionFrequency = 1000 * 2;

    private int maxExceptionTimes = 20;

    private ConcurrentMap<String, ExceptionInfo> exceptionInfoMap = new ConcurrentHashMap<>();

    public ExceptionPolicy(long minExceptionFrequency, int maxExceptionTimes) {
        this.minExceptionFrequency = minExceptionFrequency;
        this.maxExceptionTimes = maxExceptionTimes;
    }

    /**
     * 记录报错信息
     * @param ip ip
     * @param port  端口
     */
    private void recordException(String ip, int port){
        String key = getKey(ip, port);
        synchronized (key.intern()){
            ExceptionInfo exceptionInfo = exceptionInfoMap.get(key);
            if(exceptionInfo == null){
                exceptionInfo = new ExceptionInfo();
                exceptionInfoMap.put(key, exceptionInfo);
            }
            exceptionInfo.increaseTimes();
            exceptionInfo.updateAccessTime();
        }
    }

    /**
     * 是否需要改变客户端(此服务的客户端已不可用)
     * @param ip    ip
     * @param port  端口
     * @return
     */
    public boolean needChangeClient(String ip, int port){
        recordException(ip, port);
        String key = getKey(ip, port);
        boolean need = false;
        synchronized (key.intern()) {
            if(exceptionInfoMap.containsKey(key)){
                ExceptionInfo exceptionInfo = exceptionInfoMap.get(key);
                long fistTime = exceptionInfo.firstExceptionTime;
                long lastTime = exceptionInfo.lastExceptionTime;
                int times = exceptionInfo.exceptionTimes;
                long frequency = (lastTime - fistTime) / times;
                if(frequency < minExceptionFrequency || times >= maxExceptionTimes){
                    need = true;
                    exceptionInfoMap.remove(key);
                }
            }
        }
        return need;
    }

    private String getKey(String ip, int port){
        return ip + ":" + port;
    }



    class ExceptionInfo{
        private int exceptionTimes = 0;
        private long firstExceptionTime = 0;
        private long lastExceptionTime = 0;

        public ExceptionInfo() {
            this.firstExceptionTime = System.currentTimeMillis();
        }

        private void increaseTimes(){
            exceptionTimes++;
        }

        private void updateAccessTime(){
            lastExceptionTime = System.currentTimeMillis();
        }

    }


}