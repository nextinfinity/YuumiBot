package ch.kalunight.yuumi;

import java.awt.Color;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.menu.SelectionDialog;

import ch.kalunight.yuumi.command.LanguageCommand;
import ch.kalunight.yuumi.model.config.ServerConfiguration;
import ch.kalunight.yuumi.model.config.option.CleanChannelOption.CleanChannelOptionInfo;
import ch.kalunight.yuumi.model.dto.DTO;
import ch.kalunight.yuumi.repositories.ConfigRepository;
import ch.kalunight.yuumi.repositories.InfoChannelRepository;
import ch.kalunight.yuumi.repositories.PlayerRepository;
import ch.kalunight.yuumi.repositories.RankHistoryChannelRepository;
import ch.kalunight.yuumi.repositories.ServerRepository;
import ch.kalunight.yuumi.service.infochannel.InfoPanelRefresher;
import ch.kalunight.yuumi.translation.LanguageManager;
import ch.kalunight.yuumi.util.CommandUtil;
import ch.kalunight.yuumi.util.EventListenerUtil;
import ch.kalunight.yuumi.util.LanguageUtil;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventListener extends ListenerAdapter {

	//Useless to translate
	private static final String WELCOME_MESSAGE = "Hello ! Thank you for adding me to your server ! "
			+ "I'm here to help you to configurate your server with some "
			+ "basic options. You can always do the command `>setup` or `>help` if you need help.\n\n"
			+ "First, please choose your language. (Will be defined for the server, i only speak in english in private message)";

	private static final Logger logger = LoggerFactory.getLogger(EventListener.class);

	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		try {
			Member owner = event.getGuild().retrieveOwner().complete();

			if(!owner.getUser().getId().equals(Yuumi.getJda().getSelfUser().getId())) {
				DTO.Server server = ServerRepository.getServerWithGuildId(event.getGuild().getIdLong());
				if(server == null) {
					ServerRepository.createNewServer(event.getGuild().getIdLong(), LanguageManager.DEFAULT_LANGUAGE);
					askingConfig(event.getGuild(), owner.getUser());
				}else {
					CommandUtil.getFullSpeakableChannel(
							event.getGuild()).sendMessage(LanguageManager.getText(server.serv_language, "guildJoinHiAgain")).queue();
				}
			}
		}catch(SQLException e) {
			logger.error("SQL error when joining a guild !", e);
		}catch(Exception e) {
			logger.error("Unknown error when joining a guild !", e);
		}
	}

	private void askingConfig(Guild guild, User owner) throws SQLException {

		DTO.Server server = ServerRepository.getServerWithGuildId(guild.getIdLong());

		MessageChannel channel;

		MessageChannel channelOfGuild = CommandUtil.getFullSpeakableChannel(guild);

		if(channelOfGuild != null) {
			channel = channelOfGuild;
		}else {
			channel = owner.openPrivateChannel().complete();
		}

		channel.sendMessage(WELCOME_MESSAGE).complete();

		SelectionDialog.Builder builder = new SelectionDialog.Builder()
				.setTimeout(60, TimeUnit.MINUTES)
				.setColor(Color.GREEN)
				.useLooping(true)
				.setSelectedEnds("**", "**")
				.setEventWaiter(Yuumi.getEventWaiter());

		List<String> langagesList = new ArrayList<>();
		List<String> translatedLanguageList = new ArrayList<>();
		for(String langage : LanguageManager.getListlanguages()) {
			builder.addChoices(LanguageManager.getText(langage, LanguageCommand.NATIVE_LANGUAGE_TRANSLATION_ID)
					+ " " + LanguageManager.getPourcentageTranslated(langage));
			translatedLanguageList.add(LanguageManager.getText(langage, LanguageCommand.NATIVE_LANGUAGE_TRANSLATION_ID));
			langagesList.add(langage);
		}

		builder.setText(LanguageUtil.getUpdateMessageAfterChangeSelectAction(LanguageManager.DEFAULT_LANGUAGE, translatedLanguageList));
		builder.setSelectionConsumer(EventListenerUtil.getSelectionDoneActionLangueSelection(langagesList, server, channel));
		builder.setCanceled(LanguageUtil.getCancelActionSelection());

		builder.build().display(channel);
	}

	@Override
	public void onTextChannelDelete(TextChannelDeleteEvent event) {
		try {
			DTO.InfoChannel infochannel = InfoChannelRepository.getInfoChannel(event.getGuild().getIdLong());
			if(infochannel != null && infochannel.infochannel_channelid == event.getChannel().getIdLong()) {
				InfoChannelRepository.deleteInfoChannel(ServerRepository.getServerWithGuildId(event.getGuild().getIdLong()));
			}

			DTO.RankHistoryChannel rankChannel = RankHistoryChannelRepository.getRankHistoryChannel(event.getGuild().getIdLong());
			if(rankChannel != null && rankChannel.rhChannel_channelId == event.getChannel().getIdLong()) {
				RankHistoryChannelRepository.deleteRankHistoryChannel(rankChannel.rhChannel_id);
			}
		}catch(SQLException e) {
			logger.error("Issue with db when reacting to the textChannelDelete Event.", e);
		}
	}

	@Override
	public void onRoleDelete(RoleDeleteEvent event) {
		try {
			ConfigRepository.getServerConfiguration(event.getGuild().getIdLong());
		} catch(SQLException e) {
			logger.error("Issue with db when reacting to the RoleDeleteEvent event.", e);
		}
	}

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		if(event == null) {
			return;
		}

		try {
			DTO.Player player = PlayerRepository.getPlayer(event.getGuild().getIdLong(), event.getUser().getIdLong());

			if(player != null) {
				PlayerRepository.updateTeamOfPlayerDefineNull(player.player_id);
				PlayerRepository.deletePlayer(player, event.getGuild().getIdLong());
				logger.info("Player (Discord Id {}) deleted from the guild {}", player.player_discordId, event.getGuild().getIdLong());
			}
		}catch(SQLException e) {
			logger.error("Issue with db when reacting to the GuildMemberLeaveEvent event.", e);
		}
	}

	@Override
	public void onUserActivityStart(UserActivityStartEvent event) {
		if(Yuumi.getJda().getPresence().getStatus() != OnlineStatus.DO_NOT_DISTURB) {
			try {
				if(event == null || event.getNewActivity() == null) {
					return;
				}

				Activity activity = event.getNewActivity();

				if(activity.isRich() && EventListenerUtil.checkIfIsGame(activity.asRichPresence()) && event.getGuild() != null) {
					DTO.Server server = ServerRepository.getServerWithGuildId(event.getGuild().getIdLong());

					if(server == null) {
						return;
					}

					DTO.Player registeredPlayer = PlayerRepository.getPlayer(event.getGuild().getIdLong(), event.getUser().getIdLong());
					DTO.InfoChannel infoChannel = InfoChannelRepository.getInfoChannel(event.getGuild().getIdLong());
					DTO.RankHistoryChannel rankChannel = RankHistoryChannelRepository.getRankHistoryChannel(event.getGuild().getIdLong());

					if((infoChannel != null || rankChannel != null) && registeredPlayer != null && !ServerData.isServerWillBeTreated(server)
							&& server.serv_lastRefresh.isBefore(LocalDateTime.now().minusSeconds(5))) {

						ServerData.getServersIsInTreatment().put(event.getGuild().getId(), true);
						ServerRepository.updateTimeStamp(server.serv_guildId, LocalDateTime.now());
						ServerData.getServerExecutor().execute(new InfoPanelRefresher(server, true));
					}
				}
			}catch(SQLException e) {
				logger.error("SQL Error when treating discord status update event !", e);
			}catch(Exception e) {
				logger.error("Unknown Error when treating discord status update event !", e);
			}
		}
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if(event == null || event.getMessage() == null) {
			return;
		}

		ServerConfiguration config;
		try {
			config = ConfigRepository.getServerConfiguration(event.getGuild().getIdLong());
		} catch(SQLException e) {
			logger.error("SQL Error when treating message receive !", e);
			return;
		}

		if(config.getCleanChannelOption().getCleanChannelOption().equals(CleanChannelOptionInfo.DISABLE)) {
			return;
		}

		if(event.getAuthor().equals(Yuumi.getJda().getSelfUser()) && event.getMessage().getContentRaw().startsWith("Info : From now on,")) {
			return;
		}

		Member member = event.getGuild().retrieveMemberById(event.getAuthor().getIdLong()).complete();

		if(member.getUser() != Yuumi.getJda().getSelfUser() && member.getPermissions().contains(Permission.MANAGE_CHANNEL)) {
			return;
		}

		if(config.getCleanChannelOption().getCleanChannelOption().equals(CleanChannelOptionInfo.ONLY_YUUMI_COMMANDS)
				&& event.getChannel().equals(config.getCleanChannelOption().getCleanChannel())) {

			if(event.getMessage().getContentRaw().startsWith(Yuumi.BOT_PREFIX) || member.getUser().equals(Yuumi.getJda().getSelfUser())) {
				event.getMessage().delete().queueAfter(3, TimeUnit.SECONDS);
			}

		}else if(config.getCleanChannelOption().getCleanChannelOption().equals(CleanChannelOptionInfo.ALL)
				&& config.getCleanChannelOption().getCleanChannel().equals(event.getChannel())) {
			event.getMessage().delete().queueAfter(3, TimeUnit.SECONDS);
		}
	}
}
