package hundun.quizgame.mirai.botlogic.command;

import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.MemberCommandSender;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.utils.ExternalResource;

/**
 * 找不到一个同时代表CommandSender和Contact的类，姑且先用本类来实现
 */
public class CommandReplyReceiver {
    CommandSender commandSender;
    Contact contact;
    
    
    public CommandReplyReceiver(CommandSender commandSender) {
        this.commandSender = commandSender;
    }
    
    public CommandReplyReceiver(Contact contact) {
        this.contact = contact;
    }
    
    
    public void sendMessage(Message message) {
        if (commandSender != null) {
            commandSender.sendMessage(message);
        } else if (contact != null) {
            contact.sendMessage(message);
        }
        
    }

    public void sendMessage(String message) {
        if (commandSender != null) {
            commandSender.sendMessage(message);
        } else if (contact != null) {
            contact.sendMessage(message);
        }
    }

    public Image uploadImage(ExternalResource externalResource) {
        if (commandSender != null) {
            if (commandSender instanceof MemberCommandSender) {
                return ((MemberCommandSender)commandSender).getGroup().uploadImage(externalResource);
            }
        } else if (contact != null) {
            return contact.uploadImage(externalResource);
        }
        return null;
    }
    
    
    public boolean isFileSupported() {
        if (commandSender != null) {
            if (commandSender instanceof MemberCommandSender) {
                return true;
            }
        } else if (contact != null) {
            return true;
        }
        return false;
    }
    
}
