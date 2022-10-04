package DSBot.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import DSBot.Library;
import DSBot.exception.DSBotException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.ErrorResponse;

public class CommandMpAll implements CommandExecutor {
	
	private static Message message;
	private static MessageChannel channel;
	private static Library library;
	private static String[] args;
	
	@Override
	public void run(MessageReceivedEvent event, Command command, Library library, String[] args)
			throws InterruptedException, ClassNotFoundException, IOException, Exception {
		message = event.getMessage();
		channel = event.getChannel();
		CommandMpAll.library = library;
		CommandMpAll.args = args;
		mpall();
	}

	private void mpall() throws DSBotException {
		channel.sendTyping().queue();
		EmbedBuilder info = new EmbedBuilder();
		info.setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl());
		if(!authorizedToMPAll(message.getMember()))
			throw new DSBotException(message, "Non autorisé pour la plèbe.");
		String line = "";
		for(int i = 0 ; i < args.length; i++) line += args[i] + " ";
		int indexBeginGuilds = line.indexOf("[");
		int indexEndGuilds = line.indexOf("]");
		int indexOfBeginMessage = line.indexOf("|");
		if(indexBeginGuilds == -1)
			throw new DSBotException(message, "Caractère suivant manquant : [");
		else if(indexEndGuilds == -1)
			throw new DSBotException(message, "Caractère suivant manquant : ]");
		else if(indexOfBeginMessage == -1 )
			throw new DSBotException(message, "Caractère suivant manquant : |");
		else {
			String[] guilds = line.substring(indexBeginGuilds + 1, indexEndGuilds).split(",");
			if(guilds.length == 0)
				throw new DSBotException(message, "Aucune guilde mentionnée");
			else {
				List<Role> roles = message.getGuild().getRoles();
				List<Role> rolesToMentionne = new ArrayList<>();
				for(String guild : guilds) {
					Role r = null;
					for(Role role : roles) {
						if(role.getName().toLowerCase().contains(guild.toLowerCase())) {
							r = role;
							rolesToMentionne.add(r);
							break;
						}
					}
					if(r == null)
						throw new DSBotException(message, "La guilde " + guild + " n'est pas reconnu.");
				}
				List<User> members = message.getGuild()
						.getMembers()
						.stream()
						.filter(m -> m.getRoles().stream().anyMatch(r -> rolesToMentionne.contains(r)))
						.map(member -> member.getUser())
						.toList();
				info.setTitle("Verifiez les informations suivantes avant de ping.");
				info.addField("Les guildes :", rolesToMentionne.stream().map(r -> r.getName()).collect(Collectors.joining(", ")), false);
				String msg = line.substring(indexOfBeginMessage + 1) + "\n" + "- Message envoyé par " + message.getAuthor().getName();
				info.addField("Message : ", msg, false);
				message.replyEmbeds(info.build()).setActionRow(
		                Button.of(ButtonStyle.PRIMARY, "example-bot:button:symbols:white_check_mark", "VALIDATE", Emoji.fromUnicode("\u2705")),
		                Button.of(ButtonStyle.PRIMARY, "example-bot:button:symbols:x", "CANCEL", Emoji.fromUnicode("\u274c")))
				.queue(replyMessage -> {
					library
					.getEventWaiter()
					.waitForEvent(ButtonInteractionEvent.class, 
			                e -> { return checkInteraction(e, replyMessage, message); },
			                e -> {
			                	replyMessage.editMessageComponents().setActionRows().queue();
			                	String selection = e.getComponentId().split(":")[3];
			                	if(selection.equals("white_check_mark")) {
			                		info.setColor(0x00FF00);
			                		for(User user : members) sendMessage(user, msg);
			                	} else info.setColor(0xFF0000);
			                	replyMessage.editMessageEmbeds(info.build()).queue();
			                },
			                30,
			                TimeUnit.SECONDS,
			                () -> {
			                	replyMessage.editMessageEmbeds(info.clear().setTitle("Expiration.").setColor(0xFF0000).build()).setActionRows().queue();
			                });
				});
			}
		}
	}

	private static boolean authorizedToMPAll(Member messageSenderMember) {
		if(messageSenderMember.getId().equals("257273362974375937")) return true;
		if(messageSenderMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) return true;
		if(messageSenderMember.hasPermission(Permission.KICK_MEMBERS)) return true;
		if(messageSenderMember.hasPermission(Permission.BAN_MEMBERS)) return true;
		return false;
	}
	
	private static boolean checkInteraction(ButtonInteractionEvent event, Message replyMessage, Message message) {
		if (event.getUser().getIdLong() != message.getAuthor().getIdLong()) return false;
        if (event.getMessageIdLong() != replyMessage.getIdLong()) return false;
        if (!equalsAny(event.getComponentId())) return false;
        return !event.isAcknowledged();
	}
	
    private static boolean equalsAny(String buttonId) {
        return buttonId.equals("example-bot:button:symbols:white_check_mark") ||
               buttonId.equals("example-bot:button:symbols:x");
    }
    
	public static void sendMessage(User user, String content) {
	    user.openPrivateChannel()
	        .flatMap(channel -> channel.sendMessage(content))
	        .queue(null, new ErrorHandler()
	            .handle(ErrorResponse.CANNOT_SEND_TO_USER,
	                (ex) -> System.out.println("Cannot send message to user")));
	}
}
