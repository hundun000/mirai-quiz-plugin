package hundun.quizgame.mirai.botlogic.component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import hundun.quizgame.core.service.GameService;
import hundun.quizgame.core.service.QuestionLoaderService;
import hundun.quizgame.core.service.TeamService;
import hundun.quizgame.mirai.botlogic.command.QuizCommand;
import hundun.quizgame.mirai.botlogic.data.QuizConfig;
import hundun.quizgame.mirai.plugin.QuizPlugin;
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

    private final QuizPlugin plugin;
    
    public final QuizCommand quizCommand;
    
    @Autowired
    public QuizCommandAdapter(
            QuizPlugin parent, 
            GameService quizGameService,
            TeamService teamService,
            QuestionLoaderService questionLoaderService,
            QuizConfigModel configModel
            ) {
        this.quizCommand = new QuizCommand(parent, quizGameService, teamService, questionLoaderService, configModel);
        this.plugin = parent;
    }
    

    
    
    
}
