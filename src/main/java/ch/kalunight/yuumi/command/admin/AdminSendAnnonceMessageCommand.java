package ch.kalunight.yuumi.command.admin;

import java.util.function.BiConsumer;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import ch.kalunight.yuumi.ServerData;
import ch.kalunight.yuumi.command.YuumiCommand;
import ch.kalunight.yuumi.service.AdminSendMessageService;
import ch.kalunight.yuumi.util.CommandUtil;

public class AdminSendAnnonceMessageCommand extends YuumiCommand {

	public AdminSendAnnonceMessageCommand() {
		this.name = "sendAnnonce";
		this.arguments = "Text to send";
		this.help = "Send the annonce";
		this.ownerCommand = true;
		this.hidden = true;
		this.helpBiConsumer = CommandUtil.getHelpMethodIsChildrenNoTranslation(AdminCommand.USAGE_NAME, name, arguments, help);
	}

	@Override
	protected void executeCommand(CommandEvent event) {

		if(event.getArgs().isEmpty()) {
			event.reply("Message empty !");
			return;
		}

		ServerData.getServerExecutor().execute(new AdminSendMessageService(event));
	}

	@Override
	public BiConsumer<CommandEvent, Command> getHelpBiConsumer(CommandEvent event) {
		return helpBiConsumer;
	}
}
