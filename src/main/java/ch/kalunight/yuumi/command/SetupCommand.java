package ch.kalunight.yuumi.command;

import java.util.function.BiConsumer;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import ch.kalunight.yuumi.model.dto.DTO;
import ch.kalunight.yuumi.translation.LanguageManager;
import ch.kalunight.yuumi.util.CommandUtil;
import net.dv8tion.jda.api.entities.ChannelType;

public class SetupCommand extends YuumiCommand {

	public SetupCommand() {
		this.name = "setup";
		this.help = "setupHelpMessage";
		this.ownerCommand = false;
		this.guildOnly = false;
		this.helpBiConsumer = CommandUtil.getHelpMethod(name, help);
	}

	@Override
	protected void executeCommand(CommandEvent event) {
		CommandUtil.sendTypingInFunctionOfChannelType(event);
		if(event.getChannelType().equals(ChannelType.PRIVATE)) {
			event.reply(LanguageManager.getText(LanguageManager.DEFAULT_LANGUAGE, "setupMessage"));
			return;
		}
		DTO.Server server = getServer(event.getGuild().getIdLong());
		event.reply(LanguageManager.getText(server.serv_language, "setupMessage"));
	}

	@Override
	public BiConsumer<CommandEvent, Command> getHelpBiConsumer(CommandEvent event) {
		return helpBiConsumer;
	}
}
