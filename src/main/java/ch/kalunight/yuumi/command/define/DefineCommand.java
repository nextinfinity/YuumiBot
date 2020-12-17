package ch.kalunight.yuumi.command.define;

import java.util.function.BiConsumer;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import ch.kalunight.yuumi.command.YuumiCommand;
import ch.kalunight.yuumi.model.dto.DTO;
import ch.kalunight.yuumi.translation.LanguageManager;
import ch.kalunight.yuumi.util.CommandUtil;
import net.dv8tion.jda.api.Permission;

public class DefineCommand extends YuumiCommand {

	public static final String USAGE_NAME = "define";

	public DefineCommand() {
		this.name = USAGE_NAME;
		this.aliases = new String[] {"def"};
		Permission[] permissionRequired = {Permission.MANAGE_CHANNEL};
		this.userPermissions = permissionRequired;
		Command[] commandsChildren = {new DefineInfoChannelCommand(), new DefineRankChannelCommand()};
		this.children = commandsChildren;
		this.helpBiConsumer = CommandUtil.getHelpMethodHasChildren(USAGE_NAME, commandsChildren);
	}

	@Override
	protected void executeCommand(CommandEvent event) {
		DTO.Server server = getServer(event.getGuild().getIdLong());
		event.reply(LanguageManager.getText(server.serv_language, "mainDefineCommandHelpMessage"));
	}

	@Override
	public BiConsumer<CommandEvent, Command> getHelpBiConsumer(CommandEvent event) {
		return helpBiConsumer;
	}
}
