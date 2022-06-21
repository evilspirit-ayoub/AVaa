package DSBot.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Optional;

public class CommandRegistry {
	
	private List<Command> commands = new ArrayList<>();;
	
	public void addCommand(Command command) { commands.add(command); }
	
	public void removeCommand(String id) { commands.removeIf(command -> command.getId().equalsIgnoreCase(id)); }
	
	public Optional<Command> getByAliases(String alias) {
		for(Command command : commands)
			if(Arrays.asList(command.getAliases()).contains(alias))
				return Optional.of(command);
		return Optional.absent();
	}
}
