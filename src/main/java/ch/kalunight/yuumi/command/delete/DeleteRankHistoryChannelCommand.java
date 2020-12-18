package ch.kalunight.yuumi.command.delete;

import java.sql.SQLException;
import java.util.function.BiConsumer;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import ch.kalunight.yuumi.command.YuumiCommand;
import ch.kalunight.yuumi.model.dto.DTO;
import ch.kalunight.yuumi.repositories.RankHistoryChannelRepository;
import ch.kalunight.yuumi.translation.LanguageManager;
import ch.kalunight.yuumi.util.CommandUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class DeleteRankHistoryChannelCommand extends YuumiCommand {

	public DeleteRankHistoryChannelCommand() {
		this.name = "rankChannel";
		this.arguments = "";
		Permission[] permissionRequired = {Permission.MANAGE_CHANNEL};
		this.userPermissions = permissionRequired;
		this.guildOnly = true;
		this.help = "deleteRankChannelHelpMessage";
		this.helpBiConsumer = CommandUtil.getHelpMethodIsChildren(DeleteCommand.USAGE_NAME, name, arguments, help);
	}

	@Override
	protected void executeCommand(CommandEvent event) throws SQLException {

		DTO.Server server = getServer(event.getGuild().getIdLong());

		DTO.RankHistoryChannel rankChannel = RankHistoryChannelRepository.getRankHistoryChannel(server.serv_guildId);

		if(rankChannel == null) {
			event.reply(LanguageManager.getText(server.serv_language, "deleteRankChannelNotSet"));
		} else {
			try {
				TextChannel textChannel = event.getGuild().getTextChannelById(rankChannel.rhChannel_channelId);
				if(textChannel != null) {
					textChannel.delete().queue();
				}
			} catch(InsufficientPermissionException e) {
				RankHistoryChannelRepository.deleteRankHistoryChannel(rankChannel.rhChannel_id);
				event.reply(LanguageManager.getText(server.serv_language, "deleteRankChannelDeletedMissingPermission"));
				return;
			}

			RankHistoryChannelRepository.deleteRankHistoryChannel(rankChannel.rhChannel_id);
			event.reply(LanguageManager.getText(server.serv_language, "deleteRankChannelDoneMessage"));
		}
	}

	@Override
	public BiConsumer<CommandEvent, Command> getHelpBiConsumer(CommandEvent event) {
		return helpBiConsumer;
	}

}
