package hundun.quizgame.mirai.plugin.command;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import hundun.quizgame.core.dto.buff.BuffRuntimeDTO;
import hundun.quizgame.core.dto.event.AnswerResultEvent;
import hundun.quizgame.core.dto.event.FinishEvent;
import hundun.quizgame.core.dto.event.StartMatchEvent;
import hundun.quizgame.core.dto.event.SwitchQuestionEvent;
import hundun.quizgame.core.dto.event.SwitchTeamEvent;
import hundun.quizgame.core.dto.match.AnswerType;
import hundun.quizgame.core.dto.match.MatchConfigDTO;
import hundun.quizgame.core.dto.match.MatchSituationDTO;
import hundun.quizgame.core.dto.match.MatchState;
import hundun.quizgame.core.dto.match.MatchStrategyType;
import hundun.quizgame.core.dto.question.QuestionDTO;
import hundun.quizgame.core.dto.question.ResourceType;
import hundun.quizgame.core.dto.role.RoleConstInfoDTO;
import hundun.quizgame.core.dto.team.TeamConstInfoDTO;
import hundun.quizgame.core.dto.team.TeamRuntimeInfoDTO;
import hundun.quizgame.core.exception.QuizgameException;
import hundun.quizgame.core.service.GameService;
import hundun.quizgame.core.service.QuestionLoaderService;
import hundun.quizgame.core.service.TeamService;
import hundun.quizgame.mirai.plugin.export.DemoPlugin;
import lombok.Data;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.command.CommandOwner;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.CommandSenderOnMessage;
import net.mamoe.mirai.console.command.CompositeCommand;
import net.mamoe.mirai.console.command.MemberCommandSender;
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage;
import net.mamoe.mirai.console.command.SimpleCommand;
import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext;
import net.mamoe.mirai.console.permission.Permission;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.event.ListeningStatus;
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
        
//        if (appPublicSettings.getQuizConfig() != null) {
            List<String> builtInTeamNames = Arrays.asList("红方", "白方");
            for (String builtInTeamName : builtInTeamNames) {
                if (!teamService.existTeam(builtInTeamName)) {
                    try {
                        teamService.quickRegisterTeam(builtInTeamName, Arrays.asList(), Arrays.asList(), null);
                    } catch (QuizgameException e) {
                        plugin.getLogger().error(e);
                    }
                }
            }
//        }

    }
    
    @SubCommand("开始比赛")
    public boolean start(CommandSender sender, String matchMode, String questionPackageName, String teamName) {
        SessionData sessionData = getOrCreateSessionData(sender);
        
        if (sessionData.matchSituationDTO != null) {
            sender.sendMessage("目前已在比赛中");
            return true;
        }
        
        MatchSituationDTO newSituationDTO;
        TeamInfoLevel teamInfoLevel = null;
        try {
            MatchStrategyType matchStrategyType = chineseToMatchStrategyType(matchMode);
            
            switch (matchStrategyType) {
                case ENDLESS:
                    teamInfoLevel = TeamInfoLevel.NAME;
                    break;
                default:
                    teamInfoLevel = TeamInfoLevel.NAME_SCORE;
            }
            
            
            MatchConfigDTO matchConfigDTO = new MatchConfigDTO();
            matchConfigDTO.setMatchStrategyType(matchStrategyType);
            matchConfigDTO.setTeamNames(Arrays.asList(teamName.split("&")));
            matchConfigDTO.setQuestionPackageName(questionPackageName);
            
            
            String sessionId = quizService.createMatch(matchConfigDTO).getId();   
            newSituationDTO = quizService.startMatch(sessionId);
        } catch (Exception e) {
            newSituationDTO = null;
            plugin.getLogger().error("quizService error: ", e);
        }
        
        
        if (newSituationDTO != null)  {
            sessionData.matchSituationDTO = newSituationDTO;
            sessionData.teamInfoLevel = teamInfoLevel;
            
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("开始比赛成功");
            if (sessionData.teamInfoLevel == TeamInfoLevel.NAME_SCORE) {
                
                StartMatchEvent startMatchEvent = newSituationDTO.getStartMatchEvent();
                
                String teamDetailText = teamsDetailText(newSituationDTO.getTeamRuntimeInfos(), startMatchEvent.getTeamConstInfos(), startMatchEvent.getRoleConstInfos());
                stringBuilder.append("\n\n").append(teamDetailText);
            }
            
            
            sender.sendMessage(stringBuilder.toString());
            return true;
        } else {
            sender.sendMessage("开始比赛失败");
            return true;
        }
        
        
    }
    
    private String teamsDetailText(List<TeamRuntimeInfoDTO> teamRuntimeDTOs, List<TeamConstInfoDTO> teamDTOs, List<RoleConstInfoDTO> roleDTOs) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("队伍详情:\n");
        for (int i = 0; i < teamRuntimeDTOs.size(); i++) {
            TeamRuntimeInfoDTO teamRuntimeInfoDTO = teamRuntimeDTOs.get(i);
            TeamConstInfoDTO team = teamDTOs.get(i);
            
            
            stringBuilder.append(team.getName()).append(" 生命:").append(teamRuntimeInfoDTO.getHealth()).append("\n");
            if (team.getPickTags().size() > 0) {
                stringBuilder.append("Pick:");
                team.getPickTags().forEach(tag -> stringBuilder.append(tag).append("、"));
                stringBuilder.setLength(stringBuilder.length() - 1);
                stringBuilder.append("\n");
            }
            if (team.getBanTags().size() > 0) {
                stringBuilder.append("Ban:");
                team.getBanTags().forEach(tag -> stringBuilder.append(tag).append("、"));
                stringBuilder.setLength(stringBuilder.length() - 1);
                stringBuilder.append("\n");
            }
            if (false) {
                RoleConstInfoDTO role = roleDTOs.get(i);
                stringBuilder.append("英雄:").append(role.getName()).append(" 介绍:").append(role.getDescription()).append("\n");
                for (int j = 0; j < role.getSkillNames().size(); j++) {
                    stringBuilder.append("技能").append(j + 1).append(":").append(role.getSkillNames().get(j)).append(" ");
                    stringBuilder.append("次数:").append(role.getSkillFullCounts().get(j)).append(" ");
                    stringBuilder.append("效果:").append(role.getSkillDescriptions().get(j)).append("\n");
                }
                stringBuilder.append("\n");
            }
            
        }
        return stringBuilder.toString();
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


        MatchSituationDTO newSituationDTO;
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
        
        QuestionDTO questionDTO = sessionData.matchSituationDTO.getQuestion();
        if (questionDTO.getResource().getType() == ResourceType.IMAGE) {
            String imageResourceId = questionDTO.getResource().getData();
            sessionData.resource = plugin.resolveDataFile(questionLoaderService.RESOURCE_ICON_FOLDER + File.separator + imageResourceId);
        } else {
            sessionData.resource = null;
        }
        sessionData.createTime = System.currentTimeMillis();
        StringBuilder builder = new StringBuilder();
        
        if (sessionData.teamInfoLevel == TeamInfoLevel.NAME_SCORE) {
            SwitchQuestionEvent switchQuestionEvent = newSituationDTO.getSwitchQuestionEvent();
            TeamRuntimeInfoDTO currentTeam = newSituationDTO.getTeamRuntimeInfos().get(newSituationDTO.getCurrentTeamIndex());
            builder.append("当前队伍:").append(currentTeam.getName()).append(" ");
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
    
    
    @SubCommand("回答")
    public boolean answer(CommandSender sender, String answer) {
        
        SessionData sessionData = getOrCreateSessionData(sender);
        
        if (sessionData.matchSituationDTO != null && sessionData.matchSituationDTO.getState() == MatchState.WAIT_ANSWER) {
            if (answer.equals("A") || answer.equals("B") || answer.equals("C") || answer.equals("D")) {
                String correctAnser = QuestionDTO.intToAnswerText(sessionData.matchSituationDTO.getQuestion().getAnswer());
                MatchSituationDTO newSituationDTO;
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
                    
                    
                    if (sessionData.teamInfoLevel == TeamInfoLevel.NAME_SCORE) {
                        String text = teamsNormalText(sessionData.matchSituationDTO.getTeamRuntimeInfos());
                        messageChainBuilder.add(new PlainText(text));
                    }
                    
                    if (newSituationDTO.getSwitchTeamEvent() != null) {
                        SwitchTeamEvent matchEvent = newSituationDTO.getSwitchTeamEvent();
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("\n队伍变更: ").append(matchEvent.getFromTeamName()).append(" -> ").append(matchEvent.getToTeamName());
                        messageChainBuilder.add(new PlainText(stringBuilder.toString()));
                    }
                    
                    if (newSituationDTO.getFinishEvent() != null) {
                        FinishEvent matchEvent = newSituationDTO.getFinishEvent();
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
    
    private enum TeamInfoLevel {
        NAME,
        NAME_SCORE,
        ;
    }

    @Data
    private class SessionData {
        
        MatchSituationDTO matchSituationDTO;
        File resource;
        long createTime;
        TeamInfoLevel teamInfoLevel;
    }
 
    
    private MatchStrategyType chineseToMatchStrategyType(String matchMode) throws Exception {
        switch (matchMode) {
            case "无尽模式":
                return MatchStrategyType.ENDLESS;
            case "单人模式":
                return MatchStrategyType.PRE;
            case "双人模式":
                return MatchStrategyType.MAIN;
            default:
                throw new Exception("不合法的MatchStrategyType：" + matchMode);
        }
    }
    
    private String teamsNormalText(List<TeamRuntimeInfoDTO> dtos) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("队伍状态:\n");
        for (TeamRuntimeInfoDTO dto : dtos) {
            stringBuilder.append(dto.getName()).append(" ");
            stringBuilder.append("得分:").append(dto.getMatchScore()).append(" ");
            stringBuilder.append("生命:").append(dto.getHealth()).append(" ");
            if (dto.getRuntimeBuffs().size() > 0) {
                stringBuilder.append("Buff:\n");
                for (BuffRuntimeDTO buffDTO : dto.getRuntimeBuffs()) {
                    stringBuilder.append(buffDTO.getName()).append("x").append(buffDTO.getDuration()).append(" ").append(buffDTO.getDescription()).append("\n");
                }
            }
            if (dto.getRoleRuntimeInfo() != null) {
                stringBuilder.append("英雄:").append(dto.getRoleRuntimeInfo().getName()).append(" 技能:\n");
                for (Entry<String, Integer> entry : dto.getRoleRuntimeInfo().getSkillRemainTimes().entrySet()) {
                    stringBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append(" ");
                }
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    
    
    
}
