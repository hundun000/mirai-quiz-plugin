package hundun.quizgame.mirai.plugin.botlogic;

import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import hundun.quizgame.core.service.GameService;
import hundun.quizgame.core.service.QuestionLoaderService;
import hundun.quizgame.mirai.plugin.command.QuizCommand;
import hundun.quizgame.mirai.plugin.configuration.MiraiAdaptedApplicationContext;
import hundun.quizgame.mirai.plugin.export.DemoPlugin;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.events.GroupMessageEvent;


/**
 * @author hundun
 * Created on 2021/06/29
 */
public class DemoBotLogic {
    
    DemoPlugin parent;
    
    public MiraiAdaptedApplicationContext context;
    
    QuizCommandAdapter quizCommandAdapter;
    
    public DemoBotLogic(DemoPlugin plugin) {
        this.parent = plugin;
        
        
        context = new MiraiAdaptedApplicationContext(true);
        context.registerBean(DemoPlugin.class, () -> parent);
        context.refresh();
        
        // show spring is work
        parent.getLogger().info("ApplicationContext created, has beans = " + Arrays.toString(context.getBeanDefinitionNames()));
        
        quizCommandAdapter = context.getBean(QuizCommandAdapter.class);
    }
    
    
    public void onEnable() {
        CommandManager.INSTANCE.registerCommand(quizCommandAdapter.quizCommand, false);
    }
    
    public void onDisable() {
        CommandManager.INSTANCE.unregisterCommand(quizCommandAdapter.quizCommand);
    }

    
    
}
