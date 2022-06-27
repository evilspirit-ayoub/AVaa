package DSBot.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import DSBot.Library;
import DSBot.database.model.Ladder;
import DSBot.database.model.User;
import DSBot.exception.DSBotException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandPoints implements CommandExecutor {
	
	private static Message message;
	private static MessageChannel channel;
	private static String[] args;
	
	@Override
	public void run(MessageReceivedEvent event, Command command, Library library, String[] args)
			throws InterruptedException, ClassNotFoundException, IOException, Exception {
		message = event.getMessage();
		channel = event.getChannel();
		CommandPoints.args = args;
		points();
	}

	private void points() throws DSBotException, SQLException {
		channel.sendTyping().queue();
		EmbedBuilder info = new EmbedBuilder();
		if(!authorizedToLink(message.getMember()))
			throw new DSBotException(message, "Non autorise pour la plebe.");
		info.setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl());
		if(args.length != 4)
			throw new DSBotException(message, "Pour pouvoir ajouter/enlever des points : !points add/remove pseudo points");
		else if((!args[1].equals("add") && !args[1].equals("remove")) || !isPseudo(args[2]) || !isDouble(args[3]))
			throw new DSBotException(message, "Pour pouvoir ajouter/enlever des points : !points add/remove pseudo points");
		else if(!args[1].equals("add")) {
			User user = User.getUserByPseudo(args[2]);
			if(user == null)
				throw new DSBotException(message, "Le pseudo " + args[2] + " n'est pas present dans la base de donnees.");
			else {
				user.setTotalPoints(user.getTotalPoints() - Float.parseFloat(args[3]));
				user.setMonthPoints(user.getMonthPoints() - Float.parseFloat(args[3]));
				user.update();
				Ladder.updatePoisitonsForLinkedUsers();
				Ladder.updateThisMonthLadder();
				Ladder.refreshDiscordChannelLadder(message.getGuild());
				info.setTitle("Les points ont ete retire.");
			}
		} else {
			User user = User.getUserByPseudo(args[2]);
			if(user == null)
				throw new DSBotException(message, "Le pseudo " + args[2] + " n'est pas present dans la base de donnees.");
			else {
				user.setTotalPoints(user.getTotalPoints() + Float.parseFloat(args[3]));
				user.setMonthPoints(user.getMonthPoints() + Float.parseFloat(args[3]));
				user.update();
				Ladder.updatePoisitonsForLinkedUsers();
				Ladder.updateThisMonthLadder();
				Ladder.refreshDiscordChannelLadder(message.getGuild());
				info.setTitle("Les points ont ete ajoute.");
			}
		}
		message.replyEmbeds(info.build()).queue();
	}

	private static boolean authorizedToLink(Member messageSenderMember) {
		if(messageSenderMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) return true;
		if(messageSenderMember.hasPermission(Permission.KICK_MEMBERS)) return true;
		if(messageSenderMember.hasPermission(Permission.BAN_MEMBERS)) return true;
		return false;
	}
	
	private boolean isPseudo(String pseudo) {
		Pattern pattern = Pattern.compile("^[a-zA-ZâêèéîçôoûïæÂÊÎÔÛÄËÏÖÜÀÆÇÉÈŒœÙ][a-zâêèéîçôoûïæÂÊÎÔÛÄËÏÖÜÀÆÇÉÈŒœÙ]+([-][a-zA-ZâêèéîçôoûïæÂÊÎÔÛÄËÏÖÜÀÆÇÉÈŒœÙ][a-zâêèéîçôoûïæÂÊÎÔÛÄËÏÖÜÀÆÇÉÈŒœÙ]*)*$|[A-Za-zâêèéîçôoûïæÂÊÎÔÛÄËÏÖÜÀÆÇÉÈŒœÙ][a-zâêèéîçôoûïÂÊÎÔÛÄËÏÖÜÀÆÇÉÈŒœÙ]+([-][a-zA-ZâêèéîçôoûïæÂÊÎÔÛÄËÏÖÜÀÆÇÉÈŒœÙ][a-zâêèéîçôoûïæÂÊÎÔÛÄËÏÖÜÀÆÇÉÈŒœÙ]*)*");
		Matcher matcher = pattern.matcher(pseudo);
		if(!matcher.find()) return false;
		return true;
	}
	
	private boolean isDouble(String number) {
		Pattern pattern = Pattern.compile("^(-?)(0|([1-9][0-9]*))(\\.[0-9]+)?$");
		Matcher matcher = pattern.matcher(number);
		if(!matcher.find()) return false;
		return true;
	}
}
