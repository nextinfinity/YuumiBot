package ch.kalunight.yuumi.command.remove;

import java.util.function.BiConsumer;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import ch.kalunight.yuumi.command.YuumiCommand;
import ch.kalunight.yuumi.translation.LanguageManager;
import ch.kalunight.yuumi.util.CommandUtil;

public class RemoveCommand extends YuumiCommand {

	public static final String USAGE_NAME = "remove";

	public RemoveCommand() {
		this.name = USAGE_NAME;
		Command[] commandsChildren = {new RemovePlayerToTeamCommand(), new RemoveAccountCommand()};
		this.children = commandsChildren;
		this.helpBiConsumer = CommandUtil.getHelpMethodHasChildren(USAGE_NAME, commandsChildren);
	}

	@Override
	protected void executeCommand(CommandEvent event) {
		event.reply(LanguageManager.getText(getServer(event.getGuild().getIdLong()).serv_language, "mainRemoveCommandHelpMessage"));
	}

	@Override
	public BiConsumer<CommandEvent, Command> getHelpBiConsumer(CommandEvent event) {
		return helpBiConsumer;
	}
}
