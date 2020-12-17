package ch.kalunight.yuumi.command.define;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.function.BiConsumer;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import ch.kalunight.yuumi.ServerData;
import ch.kalunight.yuumi.command.YuumiCommand;
import ch.kalunight.yuumi.model.config.ServerConfiguration;
import ch.kalunight.yuumi.model.dto.DTO;
import ch.kalunight.yuumi.repositories.ConfigRepository;
import ch.kalunight.yuumi.repositories.InfoChannelRepository;
import ch.kalunight.yuumi.repositories.ServerRepository;
import ch.kalunight.yuumi.service.infochannel.InfoPanelRefresher;
import ch.kalunight.yuumi.translation.LanguageManager;
import ch.kalunight.yuumi.util.CommandUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class DefineInfoChannelCommand extends YuumiCommand {

	public DefineInfoChannelCommand() {
		this.name = "infochannel";
		this.arguments = "#mentionOfTheChannel";
		Permission[] permissionRequired = {Permission.MANAGE_CHANNEL};
		this.userPermissions = permissionRequired;
		this.help = "defineInfoChannelHelpMessage";
		this.helpBiConsumer = CommandUtil.getHelpMethodIsChildren(DefineCommand.USAGE_NAME, name, arguments, help);
	}

	@Override
	protected void executeCommand(CommandEvent event) throws SQLException {

		DTO.Server server = getServer(event.getGuild().getIdLong());

		DTO.InfoChannel infochannel = InfoChannelRepository.getInfoChannel(server.serv_guildId);

		if(infochannel != null) {
			TextChannel textChannel = event.getGuild().getTextChannelById(infochannel.infochannel_channelid);
			event.reply(String.format(LanguageManager.getText(server.serv_language, "defineInfoChannelAlreadySet"),
					textChannel.getAsMention()));
		} else {
			if(event.getMessage().getMentionedChannels().size() != 1) {
				event.reply(LanguageManager.getText(server.serv_language, "defineInfoChannelMentionOfAChannelNeeded"));
			} else {
				TextChannel textChannel = event.getMessage().getMentionedChannels().get(0);

				if(textChannel.getGuild().getIdLong() != server.serv_guildId) {
					event.reply(LanguageManager.getText(server.serv_language, "defineInfoChannelMentionOfAChannel"));

				} else {
					if(!event.getMessage().getMentionedChannels().get(0).canTalk()) {
						event.reply(LanguageManager.getText(server.serv_language, "defineInfoChannelMissingSpeakPermission"));
					} else {
						ServerConfiguration config = ConfigRepository.getServerConfiguration(server.serv_guildId);
						if(textChannel.equals(config.getCleanChannelOption().getCleanChannel())) {
							event.reply(LanguageManager.getText(server.serv_language, "defineInfoChannelImpossibleToDefineCleanChannel"));
						}else {
							InfoChannelRepository.createInfoChannel(server.serv_id, textChannel.getIdLong());

							if(config.getYuumiRoleOption().getRole() != null) {
								CommandUtil.giveRolePermission(event.getGuild(), textChannel, config);
							}

							event.reply(LanguageManager.getText(server.serv_language, "defineInfoChannelDoneMessage"));

							Message message = textChannel.sendMessage(LanguageManager.getText(server.serv_language, "defineInfoChannelLoadingMessage"))
									.complete();

							infochannel = InfoChannelRepository.getInfoChannel(server.serv_guildId);
							InfoChannelRepository.createInfoPanelMessage(infochannel.infoChannel_id, message.getIdLong());

							if(!ServerData.isServerWillBeTreated(server)) {
								ServerData.getServersIsInTreatment().put(event.getGuild().getId(), true);
								ServerRepository.updateTimeStamp(server.serv_guildId, LocalDateTime.now());
								ServerData.getServerExecutor().execute(new InfoPanelRefresher(server, false));
							}
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
