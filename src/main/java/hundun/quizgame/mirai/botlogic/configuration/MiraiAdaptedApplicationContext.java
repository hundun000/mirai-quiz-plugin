package hundun.quizgame.mirai.botlogic.configuration;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author hundun
 * Created on 2021/07/01
 */
public class MiraiAdaptedApplicationContext extends AnnotationConfigApplicationContext {
    
    public MiraiAdaptedApplicationContext(boolean lateRefresh) {
        super();
        this.setClassLoader(this.getClass().getClassLoader());
        this.scan("hundun.quizgame.mirai.botlogic",
                "hundun.quizgame.core"
                );
        if (!lateRefresh) {
            this.refresh();
        }
    }
}
