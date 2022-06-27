package DSBot.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import DSBot.Library;
import DSBot.database.model.Ladder;
import DSBot.database.model.User;
import DSBot.exception.DSBotException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandResetMonthPoints implements CommandExecutor {
	
	private static Message message;
	
	@Override
	public void run(MessageReceivedEvent event, Command command, Library library, String[] args)
			throws InterruptedException, ClassNotFoundException, IOException, Exception {
		message = event.getMessage();
		resetMonthPoints();
	}

	private void resetMonthPoints() throws SQLException, DSBotException {
		if(!authorizedToLink(message.getMember()))
			throw new DSBotException(message, "Non autorise pour la plebe.");
		List<User> users = User.getAllUsers();
		for(User user : users) {
			user.setMonthPoints(0);
			user.setNumberDefencesMonth(0);
			user.update();
		}
		Ladder.updatePoisitonsForLinkedUsers();
		Ladder.updateThisMonthLadder();
		Ladder.refreshDiscordChannelLadder(message.getGuild());
		message.replyEmbeds(new EmbedBuilder().setTitle("Le compteur de point et defense du mois en cours ont ete reinitialise.").build()).queue();
	}
	
	private static boolean authorizedToLink(Member messageSenderMember) {
		if(messageSenderMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) return true;
		if(messageSenderMember.hasPermission(Permission.KICK_MEMBERS)) return true;
		if(messageSenderMember.hasPermission(Permission.BAN_MEMBERS)) return true;
		return false;
	}
}
