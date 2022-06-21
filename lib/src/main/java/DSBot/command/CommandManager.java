package DSBot.command;

import com.google.common.base.Optional;

import DSBot.Library;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandManager extends ListenerAdapter {
	
	public static final String PREFIX = "!";
	public static final String SCREEN = PREFIX + "screen";
	public static final String LADDER = PREFIX + "ladder";
	public static final String LINK = PREFIX + "link";
	public static final String UNLINK = PREFIX + "unlink";
	
	private static CommandRegistry commandRegistry = new CommandRegistry();
	private Library library;
	
	public CommandManager(Library library) { this.library = library; }
	
	static {
		commandRegistry.addCommand(new Command("screen", "Do ocr on prisme/percepteur combat interface screen.", new CommandScreen(), "screen", "scr"));
		commandRegistry.addCommand(new Command("stats", "Display player ladder infos.", new CommandStats(), "stats"));
		commandRegistry.addCommand(new Command("link", "Link a pseudo to mentionned discordtag.", new CommandLink(), "link"));
		commandRegistry.addCommand(new Command("unlink", "unlink a pseudo to mentionned discordtag.", new CommandUnlink(), "unlink"));
	}
	
	public void onMessageReceived(MessageReceivedEvent event) {
		String[] args = event.getMessage().getContentRaw().split(" ");
		if(args[0].length() < PREFIX.length()) return;
		String commandName = args[0].substring(PREFIX.length());
		Optional<Command> cmd = commandRegistry.getByAliases(commandName);
		if(cmd.isPresent()) {
			try { cmd.get().getCommandExecut().run(event, cmd.get(), library, args); }
			catch (Exception e) { e.printStackTrace(); }
		}
    }
}
