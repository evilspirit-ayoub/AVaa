package DSBot;

import java.io.IOException;

import javax.security.auth.login.LoginException;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import DSBot.command.CommandManager;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Library {
	
	private final EventWaiter eventWaiter = new EventWaiter();
	
    public static void main(String ... args) throws Exception {
    	// esclave 1
    	//new Library().start("token");
    }
    
    public void start(String token) throws LoginException, InterruptedException, IOException, ClassNotFoundException {
    	JDABuilder
		.createDefault(token)
		.setChunkingFilter(ChunkingFilter.ALL) // enable member chunking for all guilds
		//.setMemberCachePolicy(MemberCachePolicy.ALL)
		.enableIntents(GatewayIntent.GUILD_MEMBERS)
        .addEventListeners(new CommandManager(this), eventWaiter)
        .build()
        .awaitReady();
    }

	public EventWaiter getEventWaiter() { return eventWaiter; }
}
