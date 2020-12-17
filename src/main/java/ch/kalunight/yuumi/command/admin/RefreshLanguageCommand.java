package ch.kalunight.yuumi.command.admin;

import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import ch.kalunight.yuumi.command.YuumiCommand;
import ch.kalunight.yuumi.translation.LanguageManager;
import ch.kalunight.yuumi.util.CommandUtil;

public class RefreshLanguageCommand extends YuumiCommand {

	private static final Logger logger = LoggerFactory.getLogger(RefreshLanguageCommand.class);

	public RefreshLanguageCommand() {
		this.name = "refreshLanguage";
		this.arguments = "";
		this.help = "Refresh file language";
		this.ownerCommand = true;
		this.hidden = true;
		this.helpBiConsumer = CommandUtil.getHelpMethodIsChildrenNoTranslation(AdminCommand.USAGE_NAME, name, arguments, help);
	}

	@Override
	protected void executeCommand(CommandEvent event) {
		CommandUtil.sendTypingInFunctionOfChannelType(event);
		event.reply("Refresh translation ...");
		try {
			LanguageManager.loadTranslations();
		} catch(Exception e) {
			event.reply("Exception throw when loading translation ! Error : " + e.getMessage());
			logger.error("Exception when loading new translation", e);
			return;
		}
		event.reply("Done !");
	}

	@Override
	public BiConsumer<CommandEvent, Command> getHelpBiConsumer(CommandEvent event) {
		return helpBiConsumer;
	}
}
