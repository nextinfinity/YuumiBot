package ch.kalunight.yuumi.command.create;

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

public class CreateRankHistoryChannelCommand extends YuumiCommand {

	public CreateRankHistoryChannelCommand() {
		this.name = "rankChannel";
		this.arguments = "nameOfTheNewChannel";
		Permission[] permissionRequired = {Permission.MANAGE_CHANNEL};
		this.userPermissions = permissionRequired;
		this.guildOnly = true;
		this.help = "createRankChannelHelpMessage";
		this.helpBiConsumer = CommandUtil.getHelpMethodIsChildren(CreateCommand.USAGE_NAME, name, arguments, help);
	}


	@Override
	protected void executeCommand(CommandEvent event) throws SQLException {

		DTO.Server server = getServer(event.getGuild().getIdLong());

		String nameChannel = event.getArgs();

		if(nameChannel == null || nameChannel.equals("")) {
			event.reply(LanguageManager.getText(server.serv_language, "nameOfInfochannelNeeded"));
			return;
		}

		if(nameChannel.length() > 100) {
			event.reply(LanguageManager.getText(server.serv_language, "nameOfTheInfoChannelNeedToBeLess100Characters"));
			return;
		}

		DTO.RankHistoryChannel rankChannelDb = RankHistoryChannelRepository.getRankHistoryChannel(event.getGuild().getIdLong());

		if(rankChannelDb != null && rankChannelDb.rhChannel_channelId != 0) {
			TextChannel rankChannel = event.getGuild().getTextChannelById(rankChannelDb.rhChannel_channelId);
			if(rankChannel == null) {
				RankHistoryChannelRepository.deleteRankHistoryChannel(rankChannelDb.rhChannel_id);
			}else {
				event.reply(String.format(LanguageManager.getText(server.serv_language, "rankChannelAlreadyExist"), rankChannel.getAsMention()));
				return;
			}
		}

		TextChannel rankChannel = event.getGuild().createTextChannel(nameChannel).complete();

		ServerConfiguration serverConfiguration = ConfigRepository.getServerConfiguration(event.getGuild().getIdLong());

		if(serverConfiguration.getYuumiRoleOption().getRole() != null) {
			CommandUtil.giveRolePermission(event.getGuild(), rankChannel, serverConfiguration);
		}
		RankHistoryChannelRepository.createRankHistoryChannel(server.serv_id, rankChannel.getIdLong());

		event.reply(LanguageManager.getText(server.serv_language, "rankChannelCorrectlyCreated"));
	}

	@Override
	public BiConsumer<CommandEvent, Command> getHelpBiConsumer(CommandEvent event) {
		return helpBiConsumer;
	}
}
