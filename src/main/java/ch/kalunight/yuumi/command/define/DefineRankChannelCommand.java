package ch.kalunight.yuumi.command.define;

import java.sql.SQLException;
import java.util.function.BiConsumer;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import ch.kalunight.yuumi.command.YuumiCommand;
import ch.kalunight.yuumi.model.config.ServerConfiguration;
import ch.kalunight.yuumi.model.dto.DTO;
import ch.kalunight.yuumi.repositories.ConfigRepository;
import ch.kalunight.yuumi.repositories.RankHistoryChannelRepository;
import ch.kalunight.yuumi.translation.LanguageManager;
import ch.kalunight.yuumi.util.CommandUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;

public class DefineRankChannelCommand extends YuumiCommand {

	public DefineRankChannelCommand() {
		this.name = "rankChannel";
		this.arguments = "#mentionOfTheChannel";
		Permission[] permissionRequired = {Permission.MANAGE_CHANNEL};
		this.userPermissions = permissionRequired;
		this.help = "defineRankChannelHelpMessage";
		this.helpBiConsumer = CommandUtil.getHelpMethodIsChildren(DefineCommand.USAGE_NAME, name, arguments, help);
	}

	@Override
	protected void executeCommand(CommandEvent event) throws SQLException {
		DTO.Server server = getServer(event.getGuild().getIdLong());

		DTO.RankHistoryChannel rankChannel = RankHistoryChannelRepository.getRankHistoryChannel(server.serv_guildId);

		if(rankChannel != null) {
			TextChannel textChannel = event.getGuild().getTextChannelById(rankChannel.rhChannel_channelId);
			event.reply(String.format(LanguageManager.getText(server.serv_language, "defineInfoChannelAlreadySet"), //Translation for both channel
					textChannel.getAsMention()));
		} else {
			if(event.getMessage().getMentionedChannels().size() != 1) {
				event.reply(LanguageManager.getText(server.serv_language, "defineRankChannelMentionOfAChannelNeeded"));
			} else {
				TextChannel textChannel = event.getMessage().getMentionedChannels().get(0);

				if(textChannel.getGuild().getIdLong() != server.serv_guildId) {
					event.reply(LanguageManager.getText(server.serv_language, "defineInfoChannelMentionOfAChannel")); //Translation for both channel

				} else {
					if(!event.getMessage().getMentionedChannels().get(0).canTalk()) {
						event.reply(LanguageManager.getText(server.serv_language, "defineInfoChannelMissingSpeakPermission")); //Translation for both channel
					} else {
						ServerConfiguration config = ConfigRepository.getServerConfiguration(server.serv_guildId);
						if(textChannel.equals(config.getCleanChannelOption().getCleanChannel())) {
							event.reply(LanguageManager.getText(server.serv_language, "defineRankChannelImpossibleToDefineCleanChannel"));
						}else {
							RankHistoryChannelRepository.createRankHistoryChannel(server.serv_id, textChannel.getIdLong());

							if(config.getYuumiRoleOption().getRole() != null) {
								CommandUtil.giveRolePermission(event.getGuild(), textChannel, config);
							}

							event.reply(LanguageManager.getText(server.serv_language, "defineRankChannelDoneMessage"));
						}
					}
				}
			}
		}
	}

	@Override
	public BiConsumer<CommandEvent, Command> getHelpBiConsumer(CommandEvent event) {
		return helpBiConsumer;
	}

}