package hundun.quizgame.mirai.botlogic.command;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.jetbrains.annotations.NotNull;

import hundun.quizgame.core.exception.QuizgameException;
import hundun.quizgame.core.model.domain.Question;
import hundun.quizgame.core.prototype.event.AnswerResultEvent;
import hundun.quizgame.core.prototype.event.StartMatchEvent;
import hundun.quizgame.core.prototype.event.SwitchQuestionEvent;
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
import hundun.quizgame.mirai.botlogic.component.QuizConfigModel;
import hundun.quizgame.mirai.botlogic.data.QuizConfig;
import hundun.quizgame.mirai.botlogic.data.SessionData;
import hundun.quizgame.mirai.botlogic.data.TeamConfig;
import hundun.quizgame.mirai.plugin.QuizPlugin;
import lombok.Data;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.CompositeCommand;
import net.mamoe.mirai.console.command.MemberCommandSender;
import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.FileSupported;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.events.AbstractMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
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
public class QuizCommand extends CompositeCommand implements ListenerHost {

    private final GameService quizService;
    private final TeamService teamService;
    private final QuizPlugin plugin;
    private final QuestionLoaderService questionLoaderService;
    
    Map<String, SessionData> sessionDataMap = new HashMap<>();
    
    public QuizCommand(
            QuizPlugin plugin, 
            GameService quizGameService,
            TeamService teamService,
            QuestionLoaderService questionLoaderService,
            QuizConfigModel configModel
            ) {
        super(plugin, "quiz", new String[]{"一站到底"}, "我是QuizCommand", plugin.getParentPermission(), CommandArgumentContext.EMPTY);
        this.quizService = quizGameService;
        this.questionLoaderService = questionLoaderService;
        this.teamService = teamService;
        this.plugin = plugin;
        
        
        
        postConstruct(configModel.getQuizConfig());
    }
    
    private void postConstruct(QuizConfig config) {
        File DATA_FOLDER = plugin.resolveDataFile("quiz/question_packages/");
        File RESOURCE_ICON_FOLDER = plugin.resolveDataFile("quiz/pictures/");
        questionLoaderService.lateInitFolder(DATA_FOLDER, RESOURCE_ICON_FOLDER);
        
        List<TeamConfig> teamConfigs = config.getTeamConfigs();
        for (TeamConfig teamConfig : teamConfigs) {
            if (!teamService.existTeam(teamConfig.getName())) {
                try {
                    teamService.registerTeam(teamConfig.getName(), teamConfig.getPickTags(), teamConfig.getBanTags(), null);
                } catch (QuizgameException e) {
                    plugin.getLogger().error(e);
                }
            }
        }
    }
    
    @SubCommand("开始比赛")
    public boolean start(CommandSender sender, String matchMode, String questionPackageName, String teamName) {
        SessionData sessionData = getOrCreateSessionData(sender);
        
        if (sessionData.getMatchSituationDTO() != null) {
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
            sessionData.setMatchSituationDTO(newSituationDTO);
            sessionData.setMatchStrategyType(matchStrategyType);
            
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("开始比赛成功");
            if (sessionData.getMatchStrategyType() == MatchStrategyType.MAIN) {
                
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
            sessionData.setId(sessionId);
            sessionDataMap.put(sessionId, sessionData);
        }
        return sessionData;
    }
    
    private SessionData getOrCreateSessionData(long groupId) {
        String sessionId = String.valueOf(groupId);
        SessionData sessionData = sessionDataMap.get(sessionId);
        if (sessionData == null) {
            sessionData = new SessionData();
            sessionDataMap.put(sessionId, sessionData);
        }
        return sessionData;
    }
    
    @SubCommand("debug")
    public boolean showSessionData(CommandSender sender) {
        
        SessionData sessionData = getOrCreateSessionData(sender);
        if (sessionData.getMatchSituationDTO() == null) {
            sender.sendMessage("没有进行中的比赛");
            return true;
        } else {
            sender.sendMessage(sessionData.toString());
            return true;
        }
        
    }
    
    
    @SubCommand("结束比赛")
    public boolean exit(CommandSender sender) {
        
        SessionData sessionData = getOrCreateSessionData(sender);
        if (sessionData.getMatchSituationDTO() == null) {
            sender.sendMessage("没有进行中的比赛");
            return true;
        } else {
            sessionData.setMatchSituationDTO(null);
            sender.sendMessage("结束比赛成功");
            return true;
        }
        
    }
    
    private void removeOldQuestionTimer(SessionData sessionData) {
        if (sessionData.getQuestionTimeoutTimer() != null) {
            sessionData.getQuestionTimeoutTimer().cancel();
            sessionData.setQuestionTimeoutTimer(null);
            plugin.getLogger().info("removeOldQuestionTimer of " + sessionData.getId());
        }
    }
    
    private void setNewQuestionTimer(SessionData sessionData, CommandReplyReceiver commandReplyReceiver, int time) {
        removeOldQuestionTimer(sessionData);
        sessionData.setQuestionTimeoutTimer(new Timer("QuestionTimeoutTimer-" + sessionData.getId(), true));
        TimerTask task = new QuestionTimeoutTask(sessionData, commandReplyReceiver, this, plugin.getLogger());
        sessionData.getQuestionTimeoutTimer().schedule(task, time * 1000);
        plugin.getLogger().info("QuestionTimeoutTimer-" + sessionData.getId() + " start schedule");
    }

    @SubCommand("出题")
    public boolean nextQuestionFromCommand(CommandSender sender) {
        SessionData sessionData = getOrCreateSessionData(sender);
        return nextQuestion(sessionData, new CommandReplyReceiver(sender), sender.getUser());
    }
    public boolean nextQuestionFromEventChannel(Group group, Member member) {
        SessionData sessionData = getOrCreateSessionData(group.getId());
        return nextQuestion(sessionData, new CommandReplyReceiver(group), member);
    }
    private boolean nextQuestion(SessionData sessionData, CommandReplyReceiver subject, User senderUser) {
 
        if (sessionData.getMatchSituationDTO() == null) {
            subject.sendMessage("没有进行中的比赛");
            return true;
        } else if (sessionData.getMatchSituationDTO().getState() == MatchState.WAIT_ANSWER) {
            subject.sendMessage("上一个问题还没回答哦~");
            return true;
        }


        MatchSituationView newSituationDTO;
        try {
            newSituationDTO = quizService.nextQustion(sessionData.getMatchSituationDTO().getId());
        } catch (QuizgameException e) {
            newSituationDTO = null;
            plugin.getLogger().error("quizService error: ", e);
        }
        if (newSituationDTO != null)  {
            sessionData.setMatchSituationDTO(newSituationDTO);
        } else {
            senderUser.sendMessage("出题失败");
            return true;
        }
        
        QuestionView questionDTO = sessionData.getMatchSituationDTO().getQuestion();
        if (questionDTO.getResource().getType() == ResourceType.IMAGE) {
            String imageResourceId = questionDTO.getResource().getData();
            sessionData.setResource(plugin.resolveDataFile(questionLoaderService.RESOURCE_ICON_FOLDER + File.separator + imageResourceId));
        } else {
            sessionData.setResource(null);
        }
        //sessionData.setQuestionStartTime(System.currentTimeMillis());
        StringBuilder builder = new StringBuilder();
        
        if (sessionData.getMatchStrategyType() == MatchStrategyType.MAIN) {
            TeamRuntimeView currentTeam = newSituationDTO.getTeamRuntimeInfos().get(newSituationDTO.getCurrentTeamIndex());
            builder.append("当前队伍:").append(currentTeam.getName()).append("\n");
            
        }
        
        SwitchQuestionEvent switchQuestionEvent = newSituationDTO.getSwitchQuestionEvent();
        if (switchQuestionEvent != null) {
            builder.append("答题时间:").append(switchQuestionEvent.getTime()).append("秒\n");
            setNewQuestionTimer(sessionData, subject, switchQuestionEvent.getTime());
        }
        
        builder.append("\n").append(questionDTO.getStem()).append("\n")
        .append("A. ").append(questionDTO.getOptions().get(0)).append("\n")
        .append("B. ").append(questionDTO.getOptions().get(1)).append("\n")
        .append("C. ").append(questionDTO.getOptions().get(2)).append("\n")
        .append("D. ").append(questionDTO.getOptions().get(3)).append("\n")
        .append("\n")
        .append("发送选项字母来回答");
        
        MessageChain messageChain = new PlainText(builder.toString()).plus(new PlainText(""));

        if (subject.isFileSupported()) {
            if (sessionData.getResource() != null) {
                ExternalResource externalResource = ExternalResource.create(sessionData.getResource());
                Image image = subject.uploadImage(externalResource);
                messageChain = messageChain.plus(image);
            }
        }
     
        subject.sendMessage(messageChain);
        
        return true;

    }
    
    
    
    
    @SubCommand({"回答", "答题"})
    public boolean answerFromCommand(CommandSender sender, String answer) {
        SessionData sessionData = getOrCreateSessionData(sender);
        return answer(sessionData, new CommandReplyReceiver(sender), sender.getUser(), answer);
    }
    
    private boolean answerFromEvenChannel(Group group, User sender, String answer) {
        SessionData sessionData = getOrCreateSessionData(group.getId());
        return answer(sessionData, new CommandReplyReceiver(group), sender, answer);
    }
    
    public boolean answerFromTimeoutTask(SessionData sessionData, CommandReplyReceiver commandReplyReceiver) {
        return answer(sessionData, commandReplyReceiver, null, Question.TIMEOUT_ANSWER_TEXT);
    }
    
    private boolean isAnswerChar(String answer) {
        return answer.equals("A") || answer.equals("B") || answer.equals("C") || answer.equals("D") || answer.equals(Question.TIMEOUT_ANSWER_TEXT);
    }
    

    private boolean answer(SessionData sessionData, CommandReplyReceiver subject, User senderUser, String answer) {    
        
        if (sessionData.getMatchSituationDTO() != null && sessionData.getMatchSituationDTO().getState() == MatchState.WAIT_ANSWER) {
            if (isAnswerChar(answer)) {
                String correctAnser = TextHelper.intToAnswerText(sessionData.getMatchSituationDTO().getQuestion().getAnswer());
                MatchSituationView newSituationDTO;
                try {
                    newSituationDTO = quizService.teamAnswer(sessionData.getMatchSituationDTO().getId(), answer);
                } catch (QuizgameException e) {
                    newSituationDTO = null;
                    plugin.getLogger().error("quizService error: ", e);
                }
                
                if (newSituationDTO != null)  {
                    sessionData.setMatchSituationDTO(newSituationDTO);
                } else {
                    plugin.getLogger().warning("newSituationDTO is null after answer");
                    return false;
                }
                
                removeOldQuestionTimer(sessionData);
                
                AnswerResultEvent answerResultEvent = sessionData.getMatchSituationDTO().getAnswerResultEvent();
                
                if (answerResultEvent != null) {
                    
                    MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
                    if (senderUser != null) {
                        messageChainBuilder.add(new At(senderUser.getId()));
                    }
                    
                    {
                        StringBuilder stringBuilder = new StringBuilder();
                        if (answer.equals(Question.TIMEOUT_ANSWER_TEXT)) {
                            stringBuilder.append("本题已超时QAQ\n正确答案是" + correctAnser);
                        } else if (answerResultEvent.getResult() == AnswerType.CORRECT) {
                            stringBuilder.append("回答正确\n正确答案是" + correctAnser);
                        } else if (answerResultEvent.getResult() == AnswerType.WRONG) {
                            stringBuilder.append("回答错误QAQ\n正确答案是" + correctAnser);
                        } else if (answerResultEvent.getResult() == AnswerType.SKIPPED) {
                            stringBuilder.append("本题已跳过\n正确答案是" + correctAnser);
                        }
                        stringBuilder.append("\n").append(answerResultEvent.getAddScoreTeamName()).append(" +").append(answerResultEvent.getAddScore()).append("分\n");
                        messageChainBuilder.add(new PlainText(stringBuilder.toString()));
                    }
                    
                    
                    if (sessionData.getMatchStrategyType() == MatchStrategyType.MAIN) {
                        String text = TextHelper.teamsNormalText(sessionData.getMatchSituationDTO().getTeamRuntimeInfos());
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
                        
                        sessionData.setMatchSituationDTO(null);
                    }
                    
                    subject.sendMessage(messageChainBuilder.build());
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }
    



 
    
    /**
     * 为了让比赛更顺畅，比赛中可以不使用指令格式，而是用最简洁的文本
     * @param event
     * @throws Exception
     */
    @EventHandler
    public void onMessage(@NotNull GroupMessageEvent event) throws Exception { 
        SessionData sessionData = getOrCreateSessionData(event.getGroup().getId());
        String text = event.getMessage().contentToString();
        
        if (sessionData.getMatchSituationDTO() != null) {
            
            switch (text) {
                case "A":
                case "B":
                case "C":
                case "D":
                    
                    answerFromEvenChannel(event.getGroup(), event.getSender(), event.getMessage().contentToString());
                    
                    break;
                case "出题":
                    nextQuestionFromEventChannel(event.getGroup(), event.getSender());
                    break;
                default:
                    break;
            }
        }
        
        
    }
    

    
    
    
}
