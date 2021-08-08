package hundun.quizgame.mirai.botlogic.command;

import java.util.TimerTask;

import hundun.quizgame.core.prototype.match.MatchState;
import hundun.quizgame.core.service.GameService;
import hundun.quizgame.mirai.botlogic.data.SessionData;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.utils.MiraiLogger;

public class QuestionTimeoutTask extends TimerTask {

    final SessionData sessionData;
    final CommandReplyReceiver commandReplyReceiver;
    final QuizCommand quizCommand;
    
    final MiraiLogger logger;
    
    public QuestionTimeoutTask(SessionData sessionData, CommandReplyReceiver commandReplyReceiver, QuizCommand quizCommand, MiraiLogger logger) {
        super();
        this.sessionData = sessionData;
        this.commandReplyReceiver = commandReplyReceiver;
        this.quizCommand = quizCommand;
        this.logger = logger;
    }



    @Override
    public void run() {
        logger.info("QuestionTimeoutTask arrive");
        quizCommand.answerFromTimeoutTask(sessionData, commandReplyReceiver);
    }

}
