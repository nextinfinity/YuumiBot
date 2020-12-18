package ch.kalunight.yuumi.service;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jagrosh.jdautilities.command.CommandEvent;
import ch.kalunight.yuumi.Yuumi;
import ch.kalunight.yuumi.util.Resources;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PrivateChannel;

public class AdminSendMessageService implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(AdminSendMessageService.class);
	private CommandEvent event;

	public AdminSendMessageService(CommandEvent event) {
		this.event = event;
	}

	@Override
	public void run() {
		List<String> userAlreadySendedId = new ArrayList<>();

		for(Guild guild : Yuumi.getJda().getGuilds()) {

			try {
				if(!Resources.isBlackListed(guild.getId()) && !userAlreadySendedId.contains(guild.getOwnerId())) {
					PrivateChannel privateChannel = Yuumi.getJda().retrieveUserById(guild.getOwnerIdLong()).complete().openPrivateChannel().complete();
					List<String> messagesToSend = CommandEvent.splitMessage(event.getArgs());
					for(String message : messagesToSend) {
						privateChannel.sendMessage(message).queue();
					}
					userAlreadySendedId.add(guild.getOwnerId());
				}
			} catch(Exception e) {
				logger.warn("Error in sending of the annonce", e);
			}
		}

		event.reply("The messsage has been sended !");
	}

}