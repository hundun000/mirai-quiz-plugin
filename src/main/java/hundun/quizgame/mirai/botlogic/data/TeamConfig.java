package hundun.quizgame.mirai.botlogic.data;

import java.util.List;

import lombok.Data;

/**
 * @author hundun
 * Created on 2021/07/31
 */
@Data
public class TeamConfig {
    private String name;
    
    private List<String> pickTags;
    
    private List<String> banTags;
}
