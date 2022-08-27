package DSBot.command;

import java.io.IOException;

import DSBot.Library;
import DSBot.exception.DSBotException;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandRatio implements CommandExecutor {
	
	private static Message message;
	private static MessageChannel channel;
	
	@Override
	public void run(MessageReceivedEvent event, Command command, Library library, String[] args)
			throws InterruptedException, ClassNotFoundException, IOException, Exception {
		message = event.getMessage();
		channel = event.getChannel();
		ratio();
	}
	
	private void ratio() throws DSBotException {
		channel.sendMessage("L + ratio + wrong + what is this + get a job + unfunny +"
				+ " you fell off + never liked you anyway + cope + ur allergic to gluten +"
				+ " don't care + cringe ur a kid + literally shut the fuck up +"
				+ " galileo did it better + your avi was made in MS Excel +"
				+ " ur bf is kinda ugly + i have more subscribers + owned + ur a toddler +"
				+ " reverse double take back + u sleep in a different bedroom from your wife +"
				+ " get rekt + i said it better + u smell + copy + who asked + dead game + seethe +"
				+ " ur a coward + stay mad + you main yuumi + aired + you drive a fiat 500 +"
				+ " the hood watches xqc now + yo mama + ok +"
				+ " currently listening to rizzle kicks without u."
				+ " plus ur mind numbingly stupid plus ur voice is ronald mcdonald.").queue();
	}
}
