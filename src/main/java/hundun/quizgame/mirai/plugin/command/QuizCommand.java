package hundun.quizgame.mirai.plugin.command;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import hundun.quizgame.core.exception.QuizgameException;
import hundun.quizgame.core.prototype.event.AnswerResultEvent;
import hundun.quizgame.core.prototype.event.StartMatchEvent;
import hundun.quizgame.core.prototype.event.SwitchTeamEvent;
import hundun.quizgame.core.prototype.match.AnswerType;
import hundun.quizgame.core.prototype.match.MatchConfig;
import hundun.quizgame.core.prototype.match.MatchState;
import hundun.quizgame.core.prototype.match.MatchStrategyType;
import hundun.quizgame.core.prototype.question.ResourceType;
import hundun.quizgame.core.service.GameService;
import hundun.quizgame.core.service.QuestionLoaderService;
import hundun.quizgame.core.service.TeamService;
import hundun.quizgame.core.tool.TextHelper;
import hundun.quizgame.core.view.match.MatchSituationView;
import hundun.quizgame.core.view.question.QuestionView;
import hundun.quizgame.core.view.team.TeamRuntimeView;
import hundun.quizgame.mirai.plugin.export.DemoPlugin;
import lombok.Data;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.CompositeCommand;
import net.mamoe.mirai.console.command.MemberCommandSender;
import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

/**
 * @author hundun
 * Created on 2021/07/15
 */
public class QuizCommand extends CompositeCommand {

    private final GameService quizService;
    private final TeamService teamService;
    private final DemoPlugin plugin;
    private final QuestionLoaderService questionLoaderService;
    
    Map<String, SessionData> sessionDataMap = new HashMap<>();
    
    public QuizCommand(
            DemoPlugin parent, 
            GameService quizGameService,
            TeamService teamService,
            QuestionLoaderService questionLoaderService
            ) {
        super(parent, "quiz", new String[]{"一站到底"}, "我是QuizCommand", parent.getParentPermission(), CommandArgumentContext.EMPTY);
        this.quizService = quizGameService;
        this.questionLoaderService = questionLoaderService;
        this.teamService = teamService;
        this.plugin = parent;
    }
    
    public void postConstruct() {
        File DATA_FOLDER = plugin.resolveDataFile("quiz/question_packages/");
        File RESOURCE_ICON_FOLDER = plugin.resolveDataFile("quiz/pictures/");
        questionLoaderService.lateInitFolder(DATA_FOLDER, RESOURCE_ICON_FOLDER);
        


    }
    
    @SubCommand("开始比赛")
    public boolean start(CommandSender sender, String matchMode, String questionPackageName, String teamName) {
        SessionData sessionData = getOrCreateSessionData(sender);
        
        if (sessionData.matchSituationDTO != null) {
            sender.sendMessage("目前已在比赛中");
            return true;
        }
        
        MatchSituationView newSituationDTO;
        MatchStrategyType matchStrategyType = TextHelper.chineseToMatchStrategyType(matchMode);

        try {
            
            
            
            MatchConfig matchConfigDTO = new MatchConfig();
            matchConfigDTO.setMatchStrategyType(matchStrategyType);
            matchConfigDTO.setTeamNames(Arrays.asList(teamName.split("&")));
            matchConfigDTO.setQuestionPackageName(questionPackageName);
            
            
            String sessionId = quizService.createMatch(matchConfigDTO).getId();   
            newSituationDTO = quizService.startMatch(sessionId);
        } catch (Exception e) {
            newSituationDTO = null;
            plugin.getLogger().error("quizService error: ", e);
            return false;
        }
        
        
        if (newSituationDTO != null)  {
            sessionData.matchSituationDTO = newSituationDTO;
            sessionData.matchStrategyType = matchStrategyType;
            
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("开始比赛成功");
            if (sessionData.matchStrategyType == MatchStrategyType.MAIN) {
                
                StartMatchEvent startMatchEvent = newSituationDTO.getStartMatchEvent();
                
                String teamDetailText = TextHelper.teamsDetailText(newSituationDTO.getTeamRuntimeInfos(), startMatchEvent.getTeamPrototypes());
                stringBuilder.append("\n\n").append(teamDetailText);
            }
            
            
            sender.sendMessage(stringBuilder.toString());
            return true;
        } else {
            sender.sendMessage("开始比赛失败");
            return true;
        }
        
        
    }
    
    
    
    private SessionData getOrCreateSessionData(CommandSender sender) {
        String sessionId = "default";
        if (sender instanceof MemberCommandSender) {
            sessionId = String.valueOf(((MemberCommandSender)sender).getGroup().getId());
        }
        
        SessionData sessionData = sessionDataMap.get(sessionId);
        if (sessionData == null) {
            sessionData = new SessionData();
            sessionDataMap.put(sessionId, sessionData);
        }
        return sessionData;
    }
    
    @SubCommand("结束比赛")
    public boolean exit(CommandSender sender) {
        
        SessionData sessionData = getOrCreateSessionData(sender);
        if (sessionData.matchSituationDTO == null) {
            sender.sendMessage("没有进行中的比赛");
            return true;
        } else {
            sessionData.matchSituationDTO = null;
            sender.sendMessage("结束比赛成功");
            return true;
        }
        
    }

    @SubCommand("出题")
    public boolean nextQuestion(CommandSender sender) {

        SessionData sessionData = getOrCreateSessionData(sender);
        
        if (sessionData.matchSituationDTO == null) {
            sender.sendMessage("没有进行中的比赛");
            return true;
        } else if (sessionData.matchSituationDTO.getState() == MatchState.WAIT_ANSWER) {
            sender.sendMessage("上一个问题还没回答哦~");
            return true;
        }


        MatchSituationView newSituationDTO;
        try {
            newSituationDTO = quizService.nextQustion(sessionData.matchSituationDTO.getId());
        } catch (QuizgameException e) {
            newSituationDTO = null;
            plugin.getLogger().error("quizService error: ", e);
        }
        if (newSituationDTO != null)  {
            sessionData.matchSituationDTO = newSituationDTO;
        } else {
            sender.sendMessage("出题失败");
            return true;
        }
        
        QuestionView questionDTO = sessionData.matchSituationDTO.getQuestion();
        if (questionDTO.getResource().getType() == ResourceType.IMAGE) {
            String imageResourceId = questionDTO.getResource().getData();
            sessionData.resource = plugin.resolveDataFile(questionLoaderService.RESOURCE_ICON_FOLDER + File.separator + imageResourceId);
        } else {
            sessionData.resource = null;
        }
        sessionData.createTime = System.currentTimeMillis();
        StringBuilder builder = new StringBuilder();
        
        if (sessionData.matchStrategyType == MatchStrategyType.MAIN) {
            //SwitchQuestionEvent switchQuestionEvent = newSituationDTO.getSwitchQuestionEvent();
            TeamRuntimeView currentTeam = newSituationDTO.getTeamRuntimeInfos().get(newSituationDTO.getCurrentTeamIndex());
            builder.append("当前队伍:").append(currentTeam.getName()).append("\n");
            //builder.append("时间:").append(switchQuestionEvent.getTime()).append("秒\n\n");
        }
        
        builder.append(questionDTO.getStem()).append("\n")
        .append("A. ").append(questionDTO.getOptions().get(0)).append("\n")
        .append("B. ").append(questionDTO.getOptions().get(1)).append("\n")
        .append("C. ").append(questionDTO.getOptions().get(2)).append("\n")
        .append("D. ").append(questionDTO.getOptions().get(3)).append("\n")
        .append("\n")
        .append("发送选项字母来回答");
        
        MessageChain messageChain = new PlainText(builder.toString()).plus(new PlainText(""));
        if (sender instanceof MemberCommandSender) {
            if (sessionData.resource != null) {
                ExternalResource externalResource = ExternalResource.create(sessionData.resource);
                Image image = sender.getSubject().uploadImage(externalResource);
                messageChain = messageChain.plus(image);
            }
        }
        
        sender.sendMessage(messageChain);
        return true;

    }
    
    
    @SubCommand({"回答", "答题"})
    public boolean answer(CommandSender sender, String answer) {
        
        SessionData sessionData = getOrCreateSessionData(sender);
        
        if (sessionData.matchSituationDTO != null && sessionData.matchSituationDTO.getState() == MatchState.WAIT_ANSWER) {
            if (answer.equals("A") || answer.equals("B") || answer.equals("C") || answer.equals("D")) {
                String correctAnser = TextHelper.intToAnswerText(sessionData.matchSituationDTO.getQuestion().getAnswer());
                MatchSituationView newSituationDTO;
                try {
                    newSituationDTO = quizService.teamAnswer(sessionData.matchSituationDTO.getId(), answer);
                } catch (QuizgameException e) {
                    newSituationDTO = null;
                    plugin.getLogger().error("quizService error: ", e);
                }
                if (newSituationDTO != null)  {
                    sessionData.matchSituationDTO = newSituationDTO;
                } else {
                    return false;
                }
                
                
                AnswerResultEvent answerResultEvent = sessionData.matchSituationDTO.getAnswerResultEvent();
                if (answerResultEvent != null) {
                    
                    MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
                    if (sender.getSubject() instanceof Member) {
                        messageChainBuilder.add(new At(sender.getSubject().getId()));
                    }
                    
                    {
                        StringBuilder stringBuilder = new StringBuilder();
                        if (answerResultEvent.getResult() == AnswerType.CORRECT) {
                            stringBuilder.append("回答正确\n正确答案是" + correctAnser);
                        } else if (answerResultEvent.getResult() == AnswerType.WRONG) {
                            stringBuilder.append("回答错误QAQ\n正确答案是" + correctAnser);
                        } else if (answerResultEvent.getResult() == AnswerType.SKIPPED) {
                            stringBuilder.append("本题已跳过\n正确答案是" + correctAnser);
                        }
                        stringBuilder.append("\n").append(answerResultEvent.getAddScoreTeamName()).append(" +").append(answerResultEvent.getAddScore()).append("分\n");
                        messageChainBuilder.add(new PlainText(stringBuilder.toString()));
                    }
                    
                    
                    if (sessionData.matchStrategyType == MatchStrategyType.MAIN) {
                        String text = TextHelper.teamsNormalText(sessionData.matchSituationDTO.getTeamRuntimeInfos());
                        messageChainBuilder.add(new PlainText(text));
                    }
                    
                    if (newSituationDTO.getSwitchTeamEvent() != null) {
                        SwitchTeamEvent matchEvent = newSituationDTO.getSwitchTeamEvent();
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("\n队伍变更: ").append(matchEvent.getFromTeamName()).append(" -> ").append(matchEvent.getToTeamName());
                        messageChainBuilder.add(new PlainText(stringBuilder.toString()));
                    }
                    
                    if (newSituationDTO.getFinishEvent() != null) {
                        //FinishEvent matchEvent = newSituationDTO.getFinishEvent();
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("\n比赛结束!");
                        messageChainBuilder.add(new PlainText(stringBuilder.toString()));
                        
                        sessionData.matchSituationDTO = null;
                    }
                    
                    sender.sendMessage(messageChainBuilder.build());
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }
    


    @Data
    private class SessionData {
        
        MatchSituationView matchSituationDTO;
        File resource;
        long createTime;
        MatchStrategyType matchStrategyType;
    }
 
    
    
    

    
    
    
}
