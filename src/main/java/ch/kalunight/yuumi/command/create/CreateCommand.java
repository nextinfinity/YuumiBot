package ch.kalunight.yuumi.command.create;

import java.util.function.BiConsumer;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import ch.kalunight.yuumi.command.YuumiCommand;
import ch.kalunight.yuumi.model.dto.DTO;
import ch.kalunight.yuumi.translation.LanguageManager;
import ch.kalunight.yuumi.util.CommandUtil;
import net.dv8tion.jda.api.Permission;

public class CreateCommand extends YuumiCommand {

	public static final String USAGE_NAME = "create";

	public CreateCommand(EventWaiter waiter) {
		this.name = USAGE_NAME;
		this.aliases = new String[] {"c"};
		Permission[] permissionRequired = {Permission.MANAGE_CHANNEL};
		this.userPermissions = permissionRequired;
		Command[] commandsChildren = {new CreateInfoChannelCommand(), new CreatePlayerCommand(), new CreateTeamCommand(),
				new CreateRankHistoryChannelCommand(), new CreateLeaderboardCommand(waiter)};
		this.children = commandsChildren;
		this.helpBiConsumer = CommandUtil.getHelpMethodHasChildren(USAGE_NAME, commandsChildren);
	}

	@Override
	protected void executeCommand(CommandEvent event) {
		DTO.Server server = getServer(event.getGuild().getIdLong());
		event.reply(LanguageManager.getText(server.serv_language, "mainCreateCommandHelpMessage"));
	}

	@Override
	public BiConsumer<CommandEvent, Command> getHelpBiConsumer(CommandEvent event) {
		return helpBiConsumer;
	}
}
