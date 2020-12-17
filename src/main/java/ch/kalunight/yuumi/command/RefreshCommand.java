package ch.kalunight.yuumi.command;

import java.util.function.BiConsumer;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import ch.kalunight.yuumi.ServerData;
import ch.kalunight.yuumi.model.dto.DTO;
import ch.kalunight.yuumi.translation.LanguageManager;
import ch.kalunight.yuumi.util.CommandUtil;

public class RefreshCommand extends YuumiCommand {

	public RefreshCommand() {
		this.name = "refresh";
		String[] aliases = {"r"};
		this.aliases = aliases;
		this.help = "refreshCommandHelp";
		this.hidden = false;
		this.ownerCommand = false;
		this.guildOnly = true;
		this.helpBiConsumer = CommandUtil.getHelpMethod(name, help);
		this.cooldown = 120;
	}

	@Override
	protected void executeCommand(CommandEvent event) {
		CommandUtil.sendTypingInFunctionOfChannelType(event);

		DTO.Server server = getServer(event.getGuild().getIdLong());

		ServerData.getServersAskedTreatment().add(server);
		event.reply(LanguageManager.getText(server.serv_language, "refreshCommandDoneMessage"));
	}

	@Override
	public BiConsumer<CommandEvent, Command> getHelpBiConsumer(CommandEvent event) {
		return helpBiConsumer;
	}
}
