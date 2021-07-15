package com.hundun.mirai.plugin.export;

import org.jetbrains.annotations.NotNull;

import com.hundun.mirai.bot.export.DemoBotLogic;

import net.mamoe.mirai.console.extension.PluginComponentStorage;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;

public class DemoPlugin extends JavaPlugin {

    public static final DemoPlugin INSTANCE = new DemoPlugin(); 
    
    DemoBotLogic botLogic;
    
    public DemoPlugin() {
        super(new JvmPluginDescriptionBuilder(
                "org.example.DemoPlugin",
                "1.0.0"
            )
            .build());
    }
    
    @Override
    public void onLoad(@NotNull PluginComponentStorage $this$onLoad) {
        botLogic = new DemoBotLogic(this);
    }
    
    @Override
    public void onEnable() {
        GlobalEventChannel.INSTANCE.registerListenerHost(botLogic);
    }

}
