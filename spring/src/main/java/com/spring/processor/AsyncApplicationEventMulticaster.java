package com.spring.processor;

import java.util.Iterator;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.AbstractApplicationEventMulticaster;
import org.springframework.core.ResolvableType;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

/**
 * 事件：ApplicationEvent 
 * 事件监听器：ApplicationListener，对监听到的事件进行处理。
 * 事件广播器：ApplicationEventMulticaster，将Springpublish的事件广播给所有的监听器。
 * Spring在ApplicationContext接口的抽象实现类AbstractApplicationContext中完成了事件体系的搭建。
 * AbstractApplicationContext拥有一个applicationEventMulticaster成员变
 * 量，applicationEventMulticaster提供了容器监听器的注册表。
 * 
 * @author hui.zhao.cfs
 *
 */
public class AsyncApplicationEventMulticaster extends AbstractApplicationEventMulticaster {
	private TaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();

	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = (taskExecutor != null ? taskExecutor : new SimpleAsyncTaskExecutor());
	}

	protected TaskExecutor getTaskExecutor() {
		return this.taskExecutor;
	}

	@SuppressWarnings("unchecked")
	public void multicastEvent(final ApplicationEvent event) {
		for (Iterator<ApplicationListener<?>> it = getApplicationListeners().iterator(); it.hasNext();) {
			final ApplicationListener listener = it.next();
			getTaskExecutor().execute(new Runnable() {
				public void run() {
					listener.onApplicationEvent(event);
				}
			});
		}
	}

	@Override
	public void multicastEvent(ApplicationEvent event, ResolvableType eventType) {

	}
}
