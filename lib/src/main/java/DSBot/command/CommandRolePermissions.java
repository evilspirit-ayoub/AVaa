package DSBot.command;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import DSBot.Library;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandRolePermissions implements CommandExecutor {

	private static Message message;
	private static MessageChannel channel;
	private static String[] args;
	@Override
	public void run(MessageReceivedEvent event, Command command, Library library, String[] args)
			throws InterruptedException, ClassNotFoundException, IOException, Exception {
		message = event.getMessage();
		channel = message.getChannel();
		CommandRolePermissions.args = args;
		role();
	}
	private void role() {
		List<Role> roles = message.getGuild().getRoles();
		EmbedBuilder info = new EmbedBuilder();
		for(Role role : roles) {
			if(role.getPermissions().size() != 0) continue;
			info.addField(role.getName(), role.getPermissions().stream().map(p -> p.toString()).collect(Collectors.joining("\n")), false);
			channel.sendMessageEmbeds(info.build()).queue();
			info.clear();
		}
	}
}
