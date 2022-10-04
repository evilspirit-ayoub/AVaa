package DSBot.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import DSBot.Library;
import DSBot.exception.DSBotException;
import io.github.bonigarcia.wdm.WebDriverManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandPortalsPositions implements CommandExecutor {
	
	private static Message message;
	private static MessageChannel channel;
	private static List<Attachment> attachments;
	private static Library library;
	private static String[] args;
	
	@Override
	public void run(MessageReceivedEvent event, Command command, Library library, String[] args) throws InterruptedException, ClassNotFoundException, IOException, Exception {
		message = event.getMessage();
		channel = message.getChannel();
		attachments = message.getAttachments();
		CommandPortalsPositions.library = library;
		CommandPortalsPositions.args = args;
		portalsPosition();
	}
	
	/*private void portalsPosition() throws InterruptedException, IOException {
		WebDriverManager.chromedriver().setup();
		ChromeOptions options = new ChromeOptions();
		
		// Fixing 255 Error crashes
		options.addArguments("--no-sandbox");
	    options.addArguments("--disable-dev-shm-usage");
	    
	    // Options to trick bot detection
	    //options.setHeadless(true);
	    options.addArguments("--disable-blink-features=AutomationControlled");
	    options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
	    options.setExperimentalOption("useAutomationExtension", false);
    
	    // Changing the user agent / browser fingerprint
	    options.addArguments("window-size=1920,1080");
	    options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36");

	    // Other
	    options.addArguments("disable-infobars");
	    
		WebDriver driver = new ChromeDriver(options);
		driver.manage().window().fullscreen();
	    try {
	      driver.get("https://www.vulbis.com");
	      driver.manage().timeouts().implicitlyWait(Duration.ofMillis(5000));
	      WebElement message = driver.findElement(By.id("exampleModalLongTitle"));
	      String value = message.getText();
	      System.out.println(value);
	      WebElement submitButton = driver.findElement(By.cssSelector("button"));
	      submitButton.click();
	    } finally {
	      //driver.quit();
	    }
	}*/
	
	private void portalsPosition() throws IOException, DSBotException {
		URL url = new URL("https://backend-dofushdv.grayroot.eu/portal/Agride");
        String res = "";
        try(BufferedReader br = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()))) {
        	String[] lines = br.readLine().replaceAll("\"", "").split("}");
        	for(int i = 0; i < 4; i++) {
        		int nameStart = lines[i].indexOf("name:") + 5;
        		int nameEnd = nameStart + lines[i].substring(lines[i].indexOf("name:")).indexOf(",") - 5;
        		String name = lines[i].substring(nameStart, nameEnd);
        		int locationStart = lines[i].indexOf("location:") + 9;
        		int locationEnd = locationStart + lines[i].substring(lines[i].indexOf("location:")).indexOf("],") + 1 - 9;
        		String location = lines[i].substring(locationStart, locationEnd);
        		int updateAtStart = lines[i].indexOf("updatedAt:") + 10;
        		String updateAt = lines[i].substring(updateAtStart);
        		Date updateDate = new Date(new Timestamp(Long.parseLong(updateAt) * 1000).getTime());
        		res += "nom : " + name +", position : " + location + ", actualisé : "+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(updateDate).toString() + "\n";
        	}
        } catch(Exception e) {
        	throw new DSBotException(message, "Service indisponible, réessayer plus tard.");
        }
        EmbedBuilder info = new EmbedBuilder();
        channel.sendMessageEmbeds(
        		new EmbedBuilder()
        		.setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl())
        		.setDescription(res)
        		.build())
        .queue();
	}
}
 