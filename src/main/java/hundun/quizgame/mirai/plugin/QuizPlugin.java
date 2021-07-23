package hundun.quizgame.mirai.plugin;

import org.jetbrains.annotations.NotNull;

import hundun.quizgame.mirai.botlogic.QuizBotLogic;
import hundun.quizgame.mirai.botlogic.QuizCommandAdapter;
import hundun.quizgame.mirai.botlogic.command.QuizCommand;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.extension.PluginComponentStorage;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;

public class QuizPlugin extends JavaPlugin {

    public static final QuizPlugin INSTANCE = new QuizPlugin(); 
    
    QuizBotLogic botLogic;
    
    public QuizPlugin() {
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
        botLogic = new QuizBotLogic(this);
        botLogic.onEnable();
    }
    
    @Override
    public void onDisable() {
        botLogic.onDisable();
        // 由GC回收即可
        botLogic = null;
    }

}
