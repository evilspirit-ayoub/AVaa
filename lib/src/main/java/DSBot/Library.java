package DSBot;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.security.auth.login.LoginException;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import DSBot.command.CommandManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;

public class Library {
	
	private final EventWaiter eventWaiter = new EventWaiter();
	
    public static void main(String ... args) throws Exception {
    	// esclave 1
<<<<<<< HEAD
    	new Library().start("OTczNTU2MDIyNDkwODU3NDg0.GFmTns.0_mRntFlUuGNuxh8_seAgTwrqgstLC-S0tQM3g");
    	// esclave 2
    	//new Library().start("OTkwMDEzMzQxNzcyNjk3NjEx.GZvfPW.GlgHzGCNwMY3Vl5s5Ju9xoUCbW1bXF8mRkoNs4");
=======
    	//new Library().start("token");
>>>>>>> 0d704eff29e44beaaa5a499d807c430181e971b2
    }
    
    public void start(String token) throws LoginException, InterruptedException, IOException, ClassNotFoundException {
    	JDA jda = JDABuilder
		.createDefault(token)
		.setChunkingFilter(ChunkingFilter.ALL) // enable member chunking for all guilds
		//.setMemberCachePolicy(MemberCachePolicy.ALL)
		.enableIntents(GatewayIntent.GUILD_MEMBERS)
        .addEventListeners(new CommandManager(this), eventWaiter)
        .build()
        .awaitReady();
    	/*new Thread() {
    		public void run() {
    			while(true) {
    				LocalDateTime now = LocalDateTime.now();
    				YearMonth yearMonthObject = YearMonth.of(now.getYear(), now.getMonth());
    				int daysInMonth = yearMonthObject.lengthOfMonth();
    				int hourRemainingBeforeNextMonth = ((daysInMonth - now.getDayOfMonth()) * 24) - now.getHour();
    				int minuteRemainingBeforeNextMonth = (hourRemainingBeforeNextMonth * 60) + 60 - now.getMinute() + 2;
    				try { Thread.sleep(minuteRemainingBeforeNextMonth * 60 * 1000); }
    				catch (InterruptedException e) { e.printStackTrace(); }
    				jda.getTextChannelById("797931372084723722")
    				.sendMessage("Nouveau mois, ladder du mois précédent : ")
    				.queue();
    				jda.getTextChannelById("797931372084723722")
    				.sendMessage("!ladder " + (LocalDate.now().getMonth().getValue() - 1) + "/" + LocalDateTime.now().getYear())
    				.queue();
    			}
    		}
    	}.start();*/
    }

	public EventWaiter getEventWaiter() { return eventWaiter; }
}
