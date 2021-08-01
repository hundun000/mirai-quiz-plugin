package hundun.quizgame.mirai.botlogic.data;

import java.io.File;

import hundun.quizgame.core.prototype.match.MatchStrategyType;
import hundun.quizgame.core.view.match.MatchSituationView;
import hundun.quizgame.mirai.botlogic.command.QuizCommand;
import lombok.Data;

/**
 * @author hundun
 * Created on 2021/07/31
 */
@Data
public class SessionData {
    MatchSituationView matchSituationDTO;
    File resource;
    long createTime;
    MatchStrategyType matchStrategyType;
}
