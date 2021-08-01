package hundun.quizgame.mirai.botlogic.component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hundun.quizgame.mirai.botlogic.data.QuizConfig;
import hundun.quizgame.mirai.plugin.QuizPlugin;

/**
 * 类似PluginCongfig。不过是手动触发保存。
 * @author hundun
 * Created on 2021/07/31
 */
@Component
public class QuizConfigModel {
    
    @Autowired
    QuizPlugin plugin;
    
    ObjectMapper objectMapper = new ObjectMapper();
    
    public static final String QUIZ_CONFIG_FILE_NAME = "quizConfig.json";
    
    File settingsFile;
    
    QuizConfig quizConfig = new QuizConfig();
    
    @PostConstruct
    public void init() {
        settingsFile = plugin.resolveConfigFile(QuizConfigModel.QUIZ_CONFIG_FILE_NAME);
        readQuizConfigFile();
    }
    
    public QuizConfig getQuizConfig() {
        return quizConfig;
    }
    
    public void saveQuizConfigFile() {
        try {
            String jsonString = objectMapper.writeValueAsString(quizConfig);
            FileOutputStream outputStream = new FileOutputStream(settingsFile);
            byte[] strToBytes = jsonString.getBytes();
            outputStream.write(strToBytes);
            outputStream.close();
        } catch (Exception e) {
            plugin.getLogger().error(e);
        }
    }
    

    private void readQuizConfigFile() {
        try {
            quizConfig = objectMapper.readValue(settingsFile, QuizConfig.class);
        } catch (IOException e) {
            plugin.getLogger().error(e);
        }
        
    }

}
