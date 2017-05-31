package com.lanshan.web.filter;

import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 检查重复提交工具类
 * @author Minko
 * 2017年5月31日
 */
public class RefreshHolder {
	private static Logger LOG = LoggerFactory.getLogger(RefreshHolder.class);
	
	
	// 保存随机序列
	private static final ConcurrentMap<String, Long> holder = new ConcurrentHashMap<String, Long>(5000);
	// 生成input项的表单元素名
	public static final String INPUT_NAME = "refresh_param";
	// holder中存储系列的数量预警值
	private static final int WARN_NUM = 300000;
	// 序列值实效的时间，单位：分钟
	private static final int INVALID_TIME = 30;
	// 预警线程检查频率，单位：秒
	private static final int CHECK_SECOND = 60*30;
	
	/**
	 * 初始化时，启动预警维护线程
	 */
	static{
		new Thread(new HolderCleaner()).start();
	}
	
	/**
	 * 生成随机序列值
	 * @return
	 */
	public static String getValue(){
		String rand = UUID.randomUUID().toString();
		holder.put(rand, System.currentTimeMillis());
		return rand;
	}
	
	/**
	 * 生成带随机序列值的input元素
	 * @return
	 */
	public static String genInputHtml(){
		String rand = UUID.randomUUID().toString();
		holder.put(rand, System.currentTimeMillis());
		return "<input name=\""+INPUT_NAME+"\" value=\""+rand+"\" type=\"hidden\">";
	}
	
	/**
	 * 生成带随机序列的值
	 * @return
	 */
	public static String genInputValue(){
		String rand = UUID.randomUUID().toString();
		holder.put(rand, System.currentTimeMillis());
		return rand;
	}
	
	/**
	 * 检查是否为刷新提交
	 * @param key
	 * @return
	 */
	public static boolean isRefresh(String key){
		Object v = holder.remove(key);
		return v == null?true:false;
	}
	
	/**
	 * 预警维护线程
	 */
	static final class HolderCleaner implements Runnable{
		@Override
		public void run() {
			while(true){
				// 达到预警存储数量时，清除过时元素
				if(holder.size() > WARN_NUM){
					long now = System.currentTimeMillis();
					for(Entry<String,Long> entry : holder.entrySet()){
						if(now - entry.getValue() > INVALID_TIME*60*1000){
							holder.remove(entry.getKey());
						}
					}
				}
				// 线程休眠
				try {
					Thread.sleep(CHECK_SECOND*1000);
				} catch (InterruptedException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
	}
	
}
