package DSBot.command;

import java.io.File;
import java.io.IOException;

import DSBot.Library;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandExecutor {
	
	/*public static final String PREFIX = "!";
	public static final String SCREEN = PREFIX + "screen";
	public static final String LADDER = PREFIX + "ladder";
	public static final String LINK = PREFIX + "link";
	public static final String UNLINK = PREFIX + "unlink";
	
	private Library library;
	
	public CommandExecutor(Library library) { this.library = library; }
	
	public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        String[] args = message.getContentRaw().split(" ");
        switch(args[0]) {
        case SCREEN :
        	try { ScreenCommand.screen(args, message); } catch (InterruptedException e1) { e1.printStackTrace(); }
        	break;
        case LADDER :
        	break;
        case LINK :
        	break;
        case UNLINK :
        	break;
        }
    }
	*/
}
