package DSBot.command;

import java.util.List;

import com.google.common.base.Optional;

import DSBot.Library;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandManager extends ListenerAdapter {
	
	public static final String PREFIX = "!";
	public static final String SCREEN = PREFIX + "screen";
	public static final String LADDER = PREFIX + "ladder";
	public static final String LINK = PREFIX + "link";
	public static final String UNLINK = PREFIX + "unlink";
	
	public static CommandRegistry commandRegistry = new CommandRegistry();
	private Library library;
	
	public CommandManager(Library library) { this.library = library; }
	
	static {
		commandRegistry.addCommand(new Command("screen", "Reconnaissance optique de caract�res sur un screen d'une interface de combat de d�fence d'un percepteur/prisme.", new CommandScreen(), "screen", "scr"));
		commandRegistry.addCommand(new Command("stats", "Affiche les statistiques de l'utilisateur.", new CommandStats(), "stats"));
		commandRegistry.addCommand(new Command("link", "Link un pseudo a l'identifiant du membre du serveur discord.", new CommandLink(), "link"));
		commandRegistry.addCommand(new Command("unlink", "Unlink un pseudo a l'identifiant du membre du serveur discord.", new CommandUnlink(), "unlink"));
		commandRegistry.addCommand(new Command("ladder", "Affiche le ladder d'un certain mois d'une certaine ann�e.", new CommandLadder(), "ladder"));
		commandRegistry.addCommand(new Command("mpall", "Envois un message priv�e aux membres des guildes mentionn�es.", new CommandMpAll(), "mpall"));
		commandRegistry.addCommand(new Command("points", "Ajoute/Retire des points a un pseudo existant dans la base de donn�es.", new CommandPoints(),"pts", "points"));
		commandRegistry.addCommand(new Command("help", "Ajoute/Retire des points � un pseudo existant dans la base de donn�es.", new CommandHelp(),"hlp", "help"));
		commandRegistry.addCommand(new Command("pet", "Pet the frizouzou", new CommandPet(),"pet"));
		commandRegistry.addCommand(new Command("resetMonthPoints", "Reinisialise les points et d�fence du mois en cours.", new CommandResetMonthPoints(),"rmp", "resetMonthPoints"));
		commandRegistry.addCommand(new Command("ratio", "Ratio le dernier message du membre cible.", new CommandRatio(),"ratio"));
		commandRegistry.addCommand(new Command("refreshClassment", "Met � jour le classement.", new CommandRefreshClassment(),"rc", "refreshClassment"));
		commandRegistry.addCommand(new Command("portalsPositions", "Renvois la positions des portails.", new CommandPortalsPositions(),"portal", "portalsPositions"));
		commandRegistry.addCommand(new Command("role", "Renvois les permissions des roles.", new CommandRolePermissions(),"role", "roles"));
	}
	
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.getTextChannel().getId().equals("738471724676284454")) { // message programme ava
			List<Emote> emote = event.getMessage().getGuild().getEmotesByName("peepoping", false);
			event.getMessage().addReaction(emote.get(0)).queue();
		}
		String[] args = event.getMessage().getContentRaw().split(" ");
		if(!args[0].startsWith(PREFIX)) return;
		String commandName = args[0].substring(PREFIX.length());
		Optional<Command> cmd = commandRegistry.getByAliases(commandName);
		if(cmd.isPresent()) {
			try { cmd.get().getCommandExecut().run(event, cmd.get(), library, args); }
			catch (Exception e) { e.printStackTrace(); }
		}
    }
}
