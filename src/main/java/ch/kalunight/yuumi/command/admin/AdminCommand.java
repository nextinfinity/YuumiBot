package ch.kalunight.yuumi.command.admin;

import java.util.function.BiConsumer;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import ch.kalunight.yuumi.command.YuumiCommand;
import ch.kalunight.yuumi.util.CommandUtil;

public class AdminCommand extends YuumiCommand {

	public static final String USAGE_NAME = "admin";

	public AdminCommand() {
		this.name = USAGE_NAME;
		this.ownerCommand = true;
		this.hidden = true;
		this.help = "Send info about admin commands";
		Command[] commandsChildren = {new AdminSendAnnonceMessageCommand(), new AdminCreateRAPIChannel(), new AdminDeleteRAPIChannel(),
				new RefreshLanguageCommand()};
		this.children = commandsChildren;
		this.helpBiConsumer = CommandUtil.getHelpMethodHasChildrenNoTranslation(name, commandsChildren);
	}

	@Override
	protected void executeCommand(CommandEvent event) {
		event.reply("Admins command, type `>admin help` for help");
	}

	@Override
	public BiConsumer<CommandEvent, Command> getHelpBiConsumer(CommandEvent event) {
		return helpBiConsumer;
	}
}
