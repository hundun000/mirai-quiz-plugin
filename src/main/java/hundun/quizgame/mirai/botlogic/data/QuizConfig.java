package hundun.quizgame.mirai.botlogic.data;

import java.util.ArrayList;
import java.util.List;


import lombok.Data;

/**
 * @author hundun
 * Created on 2021/07/14
 */
@Data
public class QuizConfig {
    //List<String> builtInTeamNames = new ArrayList<>();
    List<TeamConfig> teamConfigs;
}
