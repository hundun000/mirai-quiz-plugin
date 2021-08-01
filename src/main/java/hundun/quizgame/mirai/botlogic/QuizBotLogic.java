package hundun.quizgame.mirai.botlogic;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import hundun.quizgame.core.service.GameService;
import hundun.quizgame.core.service.QuestionLoaderService;
import hundun.quizgame.mirai.botlogic.command.QuizCommand;
import hundun.quizgame.mirai.botlogic.component.QuizCommandAdapter;
import hundun.quizgame.mirai.botlogic.configuration.MiraiAdaptedApplicationContext;
import hundun.quizgame.mirai.botlogic.data.QuizConfig;
import hundun.quizgame.mirai.plugin.QuizPlugin;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.events.GroupMessageEvent;


/**
 * @author hundun
 * Created on 2021/06/29
 */
public class QuizBotLogic {
    
    QuizPlugin plugin;
    
    public MiraiAdaptedApplicationContext context;
    
    QuizCommandAdapter quizCommandAdapter;
    
    public QuizBotLogic(QuizPlugin plugin) {
        this.plugin = plugin;
        
        
        
        context = new MiraiAdaptedApplicationContext(true);
        context.registerBean(QuizPlugin.class, () -> plugin);
        context.refresh();
        
        plugin.getLogger().info("ApplicationContext created, has beans size = " + context.getBeanDefinitionNames().length);
        quizCommandAdapter = context.getBean(QuizCommandAdapter.class);
    }
    
    
    public void onEnable() {
        CommandManager.INSTANCE.registerCommand(quizCommandAdapter.quizCommand, false);
       
        EventChannel<Event> eventChannel = GlobalEventChannel.INSTANCE.parentScope(plugin);
        eventChannel.registerListenerHost(quizCommandAdapter.quizCommand);
    }
    
    public void onDisable() {
        CommandManager.INSTANCE.unregisterCommand(quizCommandAdapter.quizCommand);
    }

    
    
    
}
