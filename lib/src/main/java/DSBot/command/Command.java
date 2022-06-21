package DSBot.command;

public class Command {

	private String id, description;
	private String[] aliases;
	private CommandExecutor commandExecut;
	
	public Command(String id, String description, CommandExecutor commandExecut, String ... aliases) {
		this.id = id;
		this.description = description;
		this.commandExecut = commandExecut;
		this.aliases = aliases;
	}
	
	public String getId() { return id; }
	public String getDescription() { return description; }
	public String[] getAliases() { return aliases; }
	public CommandExecutor getCommandExecut() { return commandExecut; }
}
