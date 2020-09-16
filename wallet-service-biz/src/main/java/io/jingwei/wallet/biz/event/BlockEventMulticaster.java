package io.jingwei.wallet.biz.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.AbstractApplicationEventMulticaster;
import org.springframework.core.ResolvableType;

import java.util.Iterator;
import java.util.concurrent.Executor;

/**
 *
 */
public class BlockEventMulticaster extends AbstractApplicationEventMulticaster {

    private Executor taskExecutor;

    public void setTaskExecutor(Executor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    protected Executor getTaskExecutor() {
        return this.taskExecutor;
    }


    /**
     * 处理消息信息
     * @param event
     */
    @Override
    public void multicastEvent(ApplicationEvent event) {
        Iterator listenerIterator = this.getApplicationListeners().iterator();
        while(listenerIterator.hasNext()) {
            final ApplicationListener listener = (ApplicationListener)listenerIterator.next();
            Executor executor = this.getTaskExecutor();
            if(executor != null) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        listener.onApplicationEvent(event);
                    }
                });
            } else {
                listener.onApplicationEvent(event);
            }
        }
    }

    public void sendEvent(ApplicationEvent event) {
        Iterator listenerIterator = this.getApplicationListeners().iterator();
        while(listenerIterator.hasNext()) {
            final ApplicationListener listener = (ApplicationListener)listenerIterator.next();
            listener.onApplicationEvent(event);
        }
    }

    @Override
    public void multicastEvent(ApplicationEvent event, ResolvableType eventType) {

    }
}
