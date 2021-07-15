package hundun.quizgame.mirai.plugin.export;

import org.jetbrains.annotations.NotNull;

import hundun.quizgame.mirai.plugin.botlogic.DemoBotLogic;
import hundun.quizgame.mirai.plugin.botlogic.QuizCommandAdapter;
import hundun.quizgame.mirai.plugin.command.QuizCommand;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.extension.PluginComponentStorage;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;

public class DemoPlugin extends JavaPlugin {

    public static final DemoPlugin INSTANCE = new DemoPlugin(); 
    
    DemoBotLogic botLogic;
    
    public DemoPlugin() {
        super(new JvmPluginDescriptionBuilder(
                "hundun.quizgame",
                "0.1.0"
            )
            .build());
    }
    
    @Override
    public void onLoad(@NotNull PluginComponentStorage $this$onLoad) {
        
    }
    
    @Override
    public void onEnable() {
        botLogic = new DemoBotLogic(this);
        botLogic.onEnable();
    }
    
    @Override
    public void onDisable() {
        botLogic.onDisable();
        // 由GC回收即可
        botLogic = null;
    }

}
