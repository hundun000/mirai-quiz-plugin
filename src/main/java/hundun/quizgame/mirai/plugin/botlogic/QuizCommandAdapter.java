package hundun.quizgame.mirai.plugin.botlogic;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import hundun.quizgame.core.service.GameService;
import hundun.quizgame.core.service.QuestionLoaderService;
import hundun.quizgame.core.service.TeamService;
import hundun.quizgame.mirai.plugin.command.QuizCommand;
import hundun.quizgame.mirai.plugin.export.DemoPlugin;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.ListenerHost;

/**
 * QuizCommand不能作为@Component，需要该类包装一下
 * @author hundun
 * Created on 2021/07/16
 */
@Component
public class QuizCommandAdapter {

    public final QuizCommand quizCommand;
   
    @Autowired
    public QuizCommandAdapter(
            DemoPlugin parent, 
            GameService quizGameService,
            TeamService teamService,
            QuestionLoaderService questionLoaderService
            ) {
        this.quizCommand = new QuizCommand(parent, quizGameService, teamService, questionLoaderService);
    }

    @PostConstruct
    public void postConstruct() {
        this.quizCommand.postConstruct();
    }
    
    
    
}
