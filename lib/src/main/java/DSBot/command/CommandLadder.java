package DSBot.command;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Month;
import java.time.Year;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import DSBot.Library;
import DSBot.database.model.Ladder;
import DSBot.exception.DSBotException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandLadder implements CommandExecutor {

	private static Message message;
	private static MessageChannel channel;
	private static String[] args;
	
	@Override
	public void run(MessageReceivedEvent event, Command command, Library library, String[] args) throws InterruptedException, ClassNotFoundException, IOException, Exception {
		message = event.getMessage();
		channel = message.getChannel();
		CommandLadder.args = args;
		ladder();
	}

	private void ladder() throws DSBotException, SQLException {
		channel.sendTyping().queue();
		if(args.length < 2) throw new DSBotException(message, "La commande doit contenir la date du ladder desire comme argument.");
		else if(!isGoodDate(args[1])) throw new DSBotException(message, "Mauvaise date, l'argument doi etre de la forme : numeroDuMois/annee");
		else {
			EmbedBuilder info = new EmbedBuilder();
			info.setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl());
			String month = Month.of(Integer.valueOf(args[1].split("/")[0])).toString();
			String year = Year.of(Integer.valueOf(args[1].split("/")[1])).toString();
			Ladder ladder = Ladder.getLadderBydDate(month + "/" + year);
			if(ladder == null) throw new DSBotException(message, "Le ladder du " + month + "/" + year + " n'existe pas.");
			else {
				String ladderStr = "";
				for(int i = 0; i < ladder.getDiscordIds().size(); i++) {
					User user = message.getGuild().getMemberById(ladder.getDiscordIds().get(i)).getUser();
					ladderStr = ladderStr + "#" + ladder.getPositions().get(i) + " " + (user == null ? ladder.getDiscordIds().get(i) : user.getName()) + " (" + ladder.getPoints().get(i) + " points)\n";
				}
				sendEmbeds(ladderStr);
			}
		}
	}

	private boolean isGoodDate(String date) {
		Pattern pattern = Pattern.compile("^\\d+$");
		Matcher matcher;
		String[] split = date.split("/");
		if(split.length != 2 || split[0].length() > 2 || split[1].length() > 4) return false;
		matcher = pattern.matcher(split[0]);
		if(!matcher.find()) return false;
		matcher = pattern.matcher(split[1]);
		if(!matcher.find()) return false;
		return true;
	}
	
	public static void sendEmbeds(String ladder) {
		if(ladder.length() == 0) return;
		EmbedBuilder info = new EmbedBuilder();
		String[] ladderLines = ladder.split("\n");
		String ladderStr = "";
		for(int i = 0; i < ladderLines.length; i++) {
			if((ladderStr.length() + ladderLines[i].length() + 1) > MessageEmbed.DESCRIPTION_MAX_LENGTH) {
				info.setDescription(ladderStr);
				channel.sendMessageEmbeds(info.build()).queue();
				info.clear();
				ladderStr = "";
			} else ladderStr = ladderStr + ladderLines[i] + "\n";
		}
		if(ladderStr.length() != 0) {
			info.setDescription(ladderStr);
			channel.sendMessageEmbeds(info.build()).queue();
		}
	}
}
