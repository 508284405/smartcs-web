package com.leyue.smartcs.common.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextHolder implements ApplicationContextAware {

  private static ApplicationContext context;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SpringContextHolder.context = applicationContext;
  }

  public static <T> T getBean(Class<T> type) {
    if (context == null) {
      throw new IllegalStateException("Spring ApplicationContext not initialized");
    }
    return context.getBean(type);
  }
}


