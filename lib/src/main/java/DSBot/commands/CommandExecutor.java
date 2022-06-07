package DSBot.commands;

import java.io.File;
import java.io.IOException;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public class CommandExecutor extends ListenerAdapter {
	
	public static final String PREFIX = "!";
	public static final String SCREEN = PREFIX + "screen";
	public static final String LADDER = PREFIX + "ladder";
	public static final String LINK = PREFIX + "link";
	public static final String UNLINK = PREFIX + "unlink";
	
	private DSBot.Library library;
	
	public CommandExecutor(DSBot.Library library) { this.library = library; }
	
	public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        String[] args = message.getContentRaw().split(" ");
        switch(args[0]) {
        case SCREEN :
        	try {
				ScreenCommand.screen(library, args, message.getGuild(), message, message.getChannel(), message.getAttachments(),message.getGuild().getName(),  new File(message.getGuild().getName() + "/" + message.getAuthor().getName()));
			} catch (InterruptedException e1) { e1.printStackTrace(); }
        	break;
        case LADDER :
        	try { LadderCommand.ladder(message.getGuild().getName(), args, message, message.getChannel()); }
        	catch (ClassNotFoundException | IOException | InterruptedException e) { e.printStackTrace(); }
        	break;
        case LINK :
        	try { LinkCommand.link(args, message.getGuild(), message.getGuild().getName(), message, message.getChannel()); }
        	catch (ClassNotFoundException | IOException | InterruptedException e) { e.printStackTrace(); }
        	break;
        case UNLINK :
        	try { UnlinkCommand.unlink(args, message.getGuild(), message.getGuild().getName(), message, message.getChannel()); }
        	catch (ClassNotFoundException | IOException | InterruptedException e) { e.printStackTrace(); }
        	break;
        }
        /*Stream.of(new File(System.getProperty("user.dir") + "/" + message.getGuild().getName() + "/").listFiles())
        .filter(file -> !file.isDirectory() && !file.getName().equals("data"))
        .forEach(file -> file.delete());*/
    }
}
