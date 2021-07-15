package com.hundun.mirai.bot.configuration;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author hundun
 * Created on 2021/07/01
 */
public class MiraiAdaptedApplicationContext extends AnnotationConfigApplicationContext {
    
    public MiraiAdaptedApplicationContext() {
        super();
        this.setClassLoader(this.getClass().getClassLoader());
        this.scan("com.hundun.mirai.bot");
        this.refresh();
    }
}
