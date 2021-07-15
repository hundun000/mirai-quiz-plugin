package com.hundun.mirai.bot.export;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.hundun.mirai.bot.configuration.MiraiAdaptedApplicationContext;
import com.hundun.mirai.bot.service.DemoService;

import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.events.GroupMessageEvent;


/**
 * @author hundun
 * Created on 2021/06/29
 */
public class DemoBotLogic implements ListenerHost {
    
    JvmPlugin parent;
    
    DemoService service;
    
    public DemoBotLogic(JvmPlugin plugin) {
        this.parent = plugin;
        
        @SuppressWarnings("resource")
        MiraiAdaptedApplicationContext context = new MiraiAdaptedApplicationContext();
        this.service = context.getBean(DemoService.class);
        
        // show spring is work
        parent.getLogger().info("ApplicationContext created, has beans = " + Arrays.toString(context.getBeanDefinitionNames()));
        parent.getLogger().info(service.check());
        
    }

    
    @NotNull
    @EventHandler
    public ListeningStatus onMessage(@NotNull GroupMessageEvent event) throws Exception { 
        // use service for event
        if (event.getMessage().contentToString().equals("test")) {
            event.getGroup().sendMessage(service.work());
        }
        return ListeningStatus.LISTENING;
    }
    
    
}
