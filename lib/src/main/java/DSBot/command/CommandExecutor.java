package DSBot.command;

import DSBot.Library;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandExecutor extends ListenerAdapter {
	
	public static final String PREFIX = "!";
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
        	try { ScreenCommand.screen(library, args, message); } catch (Exception e1) { e1.printStackTrace(); }
        	break;
        case LADDER :
        	try { LadderCommand.ladder(args, message); } catch (Exception e1) { e1.printStackTrace(); }
        	break;
        case LINK :
        	try { LinkCommand.link(args, message); } catch (Exception e1) { e1.printStackTrace(); }
        	break;
        case UNLINK :
        	try { UnlinkCommand.unlink(args, message); } catch (Exception e1) { e1.printStackTrace(); }
        	break;
        }
    }
}
