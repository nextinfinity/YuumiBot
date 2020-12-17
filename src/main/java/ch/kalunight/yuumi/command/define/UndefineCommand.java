package ch.kalunight.yuumi.command.define;

import java.sql.SQLException;
import java.util.function.BiConsumer;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import ch.kalunight.yuumi.command.YuumiCommand;
import ch.kalunight.yuumi.translation.LanguageManager;
import ch.kalunight.yuumi.util.CommandUtil;
import net.dv8tion.jda.api.Permission;

public class UndefineCommand extends YuumiCommand {

	public static final String USAGE_NAME = "undefine";

	public UndefineCommand() {
		this.name = USAGE_NAME;
		this.aliases = new String[] {"undef"};
		Permission[] permissionRequired = {Permission.MANAGE_CHANNEL};
		this.userPermissions = permissionRequired;
		Command[] commandsChildren = {new UndefineInfoChannelCommand(), new UndefineRankChannelCommand()};
		this.children = commandsChildren;
		this.helpBiConsumer = CommandUtil.getHelpMethodHasChildren(USAGE_NAME, commandsChildren);
	}

	@Override
	protected void executeCommand(CommandEvent event) throws SQLException {
		event.reply(LanguageManager.getText(
				getServer(event.getGuild().getIdLong()).serv_language, "mainUndefineCommandHelpMessage"));
	}

	@Override
	public BiConsumer<CommandEvent, Command> getHelpBiConsumer(CommandEvent event) {
		return helpBiConsumer;
	}
}
