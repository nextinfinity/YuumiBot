package ch.kalunight.yuumi.command;

import java.sql.SQLException;
import java.util.function.BiConsumer;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import ch.kalunight.yuumi.ServerData;

public class RebootCommand extends YuumiCommand {

	public RebootCommand() {
		this.name = "reboot";
		this.help = "Safely reboot the bot";
		this.hidden = true;
		this.ownerCommand = true;
		this.guildOnly = false;
	}

	@Override
	protected void executeCommand(CommandEvent event) throws SQLException {
		ServerData.setRebootAsked(true);
		event.reply("Reboot asked, will be done in next seconds ...");
	}

	@Override
	public BiConsumer<CommandEvent, Command> getHelpBiConsumer(CommandEvent event) {
		return null;
	}

}
