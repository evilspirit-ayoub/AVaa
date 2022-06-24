package DSBot.command;

import java.io.IOException;
import java.util.List;

import DSBot.Library;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandPet implements CommandExecutor {

	@Override
	public void run(MessageReceivedEvent event, Command command, Library library, String[] args) throws InterruptedException, ClassNotFoundException, IOException, Exception {
		Message message = event.getMessage();
		List<Emote> emote = message.getGuild().getEmotesByName("petthefrizouzou", false);
		if(!emote.isEmpty()) {
			String s = "";
			for(int i = 0; i < 45; i++) s += emote.get(0).getAsMention() + " ";
			message.getChannel().sendMessage(s).queue();
		}
	}
}
