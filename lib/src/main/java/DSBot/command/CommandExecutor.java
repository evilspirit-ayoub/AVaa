package DSBot.command;

import java.io.IOException;

import DSBot.Library;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface CommandExecutor {
	void run(MessageReceivedEvent event, Command command, Library library, String[] args) throws InterruptedException, ClassNotFoundException, IOException, Exception;
}
