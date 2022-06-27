package DSBot.command;

import java.io.IOException;

import DSBot.Library;
import DSBot.exception.DSBotException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandHelp implements CommandExecutor {
	private static Message message;
	private static String[] args;
	
	@Override
	public void run(MessageReceivedEvent event, Command command, Library library, String[] args)
			throws InterruptedException, ClassNotFoundException, IOException, Exception {
		message = event.getMessage();
		CommandHelp.args = args;
		help();
	}
	
	private void help() throws DSBotException {
		EmbedBuilder info = new EmbedBuilder();
		String titre = "Pour en savoir plus sur une commande specifique, entrez help suivi de la commande.";
		String description = "\n- LADDER : " + getCommand("ladder").getDescription() + "\n"
				+ "- MPALL : " + getCommand("mpall").getDescription() + "\n"
				+ "- POINTS : " + getCommand("pts").getDescription() + "\n"
				+ "- SCREEN : " + getCommand("scr").getDescription() + "\n"
				+ "- STATS : " + getCommand("stats").getDescription() + "\n"
				+ "- LINK : " + getCommand("link").getDescription() + "\n"
				+ "- UNLINK : " + getCommand("unlink").getDescription();
		if(args.length > 2)
			throw new DSBotException(message, "La commande help peut avoir au maximum un argument.");
		else if(args.length == 1) {
			info.setTitle(titre);
			info.setDescription(description);
		} else {
			switch(args[1]) {
			case "ladder" :
				info.setTitle("La commande doit etre de la forme :!ladder numeroDuMois/annee");
				break;
			case "mpall" :
				info.setTitle("La commande doit etre de la forme :!mpall [guild1,guild2,...] |message");
				break;
			case "points" :
				info.setTitle("La commande doit etre de la forme :!points add/remove pseudo points");
				break;
			case "pts" :
				info.setTitle("La commande doit etre de la forme :!points add/remove pseudo points");
				break;
			case "screen" :
				info.setTitle("La commande doit etre de la forme :!screen screen");
				break;
			case "scr" :
				info.setTitle("La commande doit etre de la forme :!screen screen");
				break;
			case "stats" :
				info.setTitle("La commande doit etre de la forme :!stats ou !stats @Discordtag");
				break;
			case "link" :
				info.setTitle("La commande doit etre de la forme :!link @Discordtag pseudo");
				break;
			case "unlink" :
				info.setTitle("La commande doit etre de la forme :!unlink @Discordtag pseudo");
				break;
			default :
				throw new DSBotException(message, "L argument " + args[1] + " est inconnu.");
			}
		}
		message.replyEmbeds(info.build()).queue();
	}

	public static Command getCommand(String name) {
		return CommandManager.commandRegistry.getByAliases(name).get();
	}
}
