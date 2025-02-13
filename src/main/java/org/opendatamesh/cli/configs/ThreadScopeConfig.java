package org.opendatamesh.cli.configs;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.SimpleThreadScope;

/**
 * Configuration class for registering a custom thread scope in the Spring application context.
 * <p>
 * This class implements {@link  BeanFactoryPostProcessor} to modify the bean factory after its standard
 * initialization. It registers a new scope named "thread" using {@link SimpleThreadScope}.
 * </p>
 * <p>
 * Beans with this scope will have a lifecycle tied to the current thread.
 * A new instance of a "thread"-scoped bean will be created for each thread that accesses it.
 * This allows those beans to have a "state" that can be accessed and modified.
 * </p>
 */
@Configuration
public class ThreadScopeConfig implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.registerScope("thread", new SimpleThreadScope());
    }
}
