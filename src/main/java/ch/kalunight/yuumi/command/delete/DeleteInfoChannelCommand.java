package ch.kalunight.yuumi.command.delete;

import java.sql.SQLException;
import java.util.function.BiConsumer;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import ch.kalunight.yuumi.command.YuumiCommand;
import ch.kalunight.yuumi.model.dto.DTO;
import ch.kalunight.yuumi.repositories.InfoChannelRepository;
import ch.kalunight.yuumi.translation.LanguageManager;
import ch.kalunight.yuumi.util.CommandUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class DeleteInfoChannelCommand extends YuumiCommand {

	public DeleteInfoChannelCommand() {
		this.name = "infoChannel";
		this.arguments = "";
		this.help = "deleteInfoChannelHelpMessage";
		Permission[] permissionRequired = {Permission.MANAGE_CHANNEL};
		this.userPermissions = permissionRequired;
		this.helpBiConsumer = CommandUtil.getHelpMethodIsChildren(DeleteCommand.USAGE_NAME, name, arguments, help);
	}

	@Override
	protected void executeCommand(CommandEvent event) throws SQLException {

		DTO.Server server = getServer(event.getGuild().getIdLong());

		DTO.InfoChannel infochannel = InfoChannelRepository.getInfoChannel(server.serv_guildId);

		if(infochannel == null) {
			event.reply(LanguageManager.getText(server.serv_language, "deleteInfoChannelChannelNotSetted"));
		} else {
			try {
				TextChannel textChannel = event.getGuild().getTextChannelById(infochannel.infochannel_channelid);
				if(textChannel != null) {
					textChannel.delete().queue();
				}
			} catch(InsufficientPermissionException e) {
				InfoChannelRepository.deleteInfoChannel(server);
				event.reply(LanguageManager.getText(server.serv_language, "deleteInfoChannelDeletedMissingPermission"));
				return;
			}

			InfoChannelRepository.deleteInfoChannel(server);
			event.reply(LanguageManager.getText(server.serv_language, "deleteInfoChannelDoneMessage"));
		}
	}

	@Override
	public BiConsumer<CommandEvent, Command> getHelpBiConsumer(CommandEvent event) {
		return helpBiConsumer;
	}
}
