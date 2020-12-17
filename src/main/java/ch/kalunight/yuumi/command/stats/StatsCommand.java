package ch.kalunight.yuumi.command.stats;

import java.sql.SQLException;
import java.util.function.BiConsumer;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import ch.kalunight.yuumi.Yuumi;
import ch.kalunight.yuumi.command.YuumiCommand;
import ch.kalunight.yuumi.model.dto.DTO;
import ch.kalunight.yuumi.translation.LanguageManager;
import ch.kalunight.yuumi.util.CommandUtil;

public class StatsCommand extends YuumiCommand {

	public static final String USAGE_NAME = "stats";

	public StatsCommand(EventWaiter waiter) {
		this.name = USAGE_NAME;
		this.aliases = new String[] {"s"};
		Command[] commandsChildren = {new StatsProfileCommand(waiter)};
		this.children = commandsChildren;
		this.helpBiConsumer = CommandUtil.getHelpMethodHasChildren(USAGE_NAME, commandsChildren);
	}

	@Override
	protected void executeCommand(CommandEvent event) throws SQLException {

		boolean playerGiven = false;
		if(!event.getMessage().getMentionedMembers().isEmpty() ||
				event.getArgs().split(" ").length >= 2) {
			playerGiven = true;
		}

		if(playerGiven) {
			for(Command command : Yuumi.getMainCommands(null)) {
				for(Command commandChild : command.getChildren()) {
					if(commandChild instanceof StatsProfileCommand) {
						((StatsProfileCommand) commandChild).executeCommand(event);
						return;
					}
				}
			}
		}

		DTO.Server server = getServer(event.getGuild().getIdLong());

		event.reply(LanguageManager.getText(server.serv_language, "mainStatsCommandHelpMessage"));
	}

	@Override
	public BiConsumer<CommandEvent, Command> getHelpBiConsumer(CommandEvent event) {
		return helpBiConsumer;
	}
}
