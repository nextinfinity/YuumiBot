package ch.kalunight.yuumi.model.config.option;

import java.awt.Color;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import ch.kalunight.yuumi.Yuumi;
import ch.kalunight.yuumi.model.dto.DTO;
import ch.kalunight.yuumi.repositories.ConfigRepository;
import ch.kalunight.yuumi.repositories.InfoChannelRepository;
import ch.kalunight.yuumi.repositories.RepoResources;
import ch.kalunight.yuumi.repositories.ServerRepository;
import ch.kalunight.yuumi.translation.LanguageManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CleanChannelOption extends ConfigurationOption {

	private static final String UNICODE_ONE = "1\u20E3";
	private static final String EMOJI_ONE = ":one:";

	private static final String UNICODE_TWO = "2\u20E3";
	private static final String EMOJI_TWO = ":two:";

	private static final String UNICODE_THREE = "3\u20E3";
	private static final String EMOJI_THREE = ":three:";

	private static final Logger logger = LoggerFactory.getLogger(CleanChannelOption.class);

	public enum CleanChannelOptionInfo {
		DISABLE("cleanChannelOptionDisable", "cleanChannelOptionDisableDesc", UNICODE_ONE, EMOJI_ONE),
		ONLY_YUUMI_COMMANDS("cleanChannelOptionYuumiCommands", "cleanChannelOptionYuumiCommandsDesc", UNICODE_TWO, EMOJI_TWO),
		ALL("cleanChannelOptionAll", "cleanChannelOptionAllDesc", UNICODE_THREE, EMOJI_THREE);

		private final String name;
		private final String description;
		private final String unicode;
		private final String emoji;

		private CleanChannelOptionInfo(String name, String description, String unicode, String emoji) {
			this.name = name;
			this.description = description;
			this.unicode = unicode;
			this.emoji = emoji;
		}
	}

	private CleanChannelOptionInfo cleanChannelOption;

	private TextChannel cleanChannel;

	/**
	 * Used when update option, this is not the state of the option.
	 */
	private CleanChannelOptionInfo tmpCleanChannelOption;

	public CleanChannelOption(long guildId) {
		super(guildId, "cleanChannelOptionDesc");
		cleanChannelOption = CleanChannelOptionInfo.DISABLE;
		cleanChannel = null;
	}

	@Override
	public Consumer<CommandEvent> getChangeConsumer(EventWaiter waiter, DTO.Server server) {
		return new Consumer<CommandEvent>() {

			@Override
			public void accept(CommandEvent event) {

				if(!event.getGuild().getSelfMember().getPermissions().contains(Permission.MESSAGE_MANAGE)) {
					event.reply(LanguageManager.getText(server.serv_language, "cleanChannelOptionPermissionNeeded"));
					return;
				}

				ButtonMenu.Builder choiceBuilder = new ButtonMenu.Builder();

				choiceBuilder.setEventWaiter(waiter);

				choiceBuilder.addChoices(
						CleanChannelOptionInfo.DISABLE.unicode,
						CleanChannelOptionInfo.ONLY_YUUMI_COMMANDS.unicode,
						CleanChannelOptionInfo.ALL.unicode,
						"❌");
				choiceBuilder.addUsers(event.getAuthor());
				choiceBuilder.setFinalAction(finalAction());
				choiceBuilder.setColor(Color.BLUE);

				choiceBuilder.setTimeout(2, TimeUnit.MINUTES);

				choiceBuilder.setText(
						String.format(LanguageManager.getText(server.serv_language, "cleanChannelOptionLongDesc"),
								LanguageManager.getText(server.serv_language, description))
								+ "\n" + CleanChannelOptionInfo.DISABLE.emoji
								+ " -> " + LanguageManager.getText(server.serv_language, CleanChannelOptionInfo.DISABLE.name)
								+ " : " + LanguageManager.getText(server.serv_language, CleanChannelOptionInfo.DISABLE.description) + "\n"
								+ CleanChannelOptionInfo.ONLY_YUUMI_COMMANDS.emoji
								+ " -> " + LanguageManager.getText(server.serv_language, CleanChannelOptionInfo.ONLY_YUUMI_COMMANDS.name)
								+ " : " + LanguageManager.getText(server.serv_language, CleanChannelOptionInfo.ONLY_YUUMI_COMMANDS.description) + "\n"
								+ CleanChannelOptionInfo.ALL.emoji
								+ " -> " + LanguageManager.getText(server.serv_language, CleanChannelOptionInfo.ALL.name)
								+ " : " + LanguageManager.getText(server.serv_language, CleanChannelOptionInfo.ALL.description) + "\n");

				choiceBuilder.setAction(updateOption(event.getChannel(), event.getGuild(), waiter, event.getAuthor(), server));

				ButtonMenu menu = choiceBuilder.build();

				menu.display(event.getChannel());
			}};
	}

	private Consumer<ReactionEmote> updateOption(MessageChannel channel, Guild guild, EventWaiter eventWaiter,
												 User user, DTO.Server server) {
		return new Consumer<ReactionEmote>() {

			@Override
			public void accept(ReactionEmote emoteUsed) {
				channel.sendTyping().complete();

				if(emoteUsed.getName().equals(CleanChannelOptionInfo.ONLY_YUUMI_COMMANDS.unicode)) {
					tmpCleanChannelOption = CleanChannelOptionInfo.ONLY_YUUMI_COMMANDS;
				}else if(emoteUsed.getName().equals(CleanChannelOptionInfo.ALL.unicode)) {
					tmpCleanChannelOption = CleanChannelOptionInfo.ALL;
				}else if(emoteUsed.getName().equals(CleanChannelOptionInfo.DISABLE.unicode)){
					tmpCleanChannelOption = CleanChannelOptionInfo.DISABLE;
				}else {
					channel.sendMessage(LanguageManager.getText(server.serv_language, "cleanChannelOptionCanceled")).queue();
					return;
				}

				if(tmpCleanChannelOption == CleanChannelOptionInfo.ONLY_YUUMI_COMMANDS
						|| tmpCleanChannelOption == CleanChannelOptionInfo.ALL) {

					ButtonMenu.Builder choiceBuilder = new ButtonMenu.Builder();

					choiceBuilder.setEventWaiter(eventWaiter);
					choiceBuilder.addChoices(UNICODE_ONE,UNICODE_TWO);
					choiceBuilder.addUsers(user);
					choiceBuilder.setFinalAction(finalAction());
					choiceBuilder.setColor(Color.BLUE);

					choiceBuilder.setTimeout(2, TimeUnit.MINUTES);

					choiceBuilder.setText(String.format(LanguageManager.getText(server.serv_language, "cleanChannelOptionChoiceChannel"),
							EMOJI_ONE, EMOJI_TWO));

					choiceBuilder.setAction(defineNewChannel(channel, guild, eventWaiter, user, server));

					ButtonMenu menu = choiceBuilder.build();

					menu.display(channel);
				}else {

					cleanChannel = null;

					try {
						ConfigRepository.updateCleanChannelOption(guildId, 0, tmpCleanChannelOption.toString());
					} catch(SQLException e) {
						RepoResources.sqlErrorReport(channel, server, e);
						return;
					}

					if(tmpCleanChannelOption.equals(cleanChannelOption)) {
						channel.sendMessage(LanguageManager.getText(server.serv_language, "cleanChannelOptionAlreadyDisable")).queue();
					}else {
						cleanChannelOption = tmpCleanChannelOption;
						channel.sendMessage(LanguageManager.getText(server.serv_language, "cleanChannelOptionNowDisable")).queue();
					}
				}
			}};
	}


	private Consumer<ReactionEmote> defineNewChannel(MessageChannel channel, Guild guild, EventWaiter eventWaiter,
													 User user, DTO.Server server) {
		return new Consumer<ReactionEmote>() {

			@Override
			public void accept(ReactionEmote reactionEmote) {
				channel.sendTyping().complete();
				if(reactionEmote.getName().equals(UNICODE_ONE)) {

					channel.sendMessage(LanguageManager.getText(server.serv_language, "cleanChannelOptionSendTextChannel")).queue();

					eventWaiter.waitForEvent(MessageReceivedEvent.class,
							e -> e.getAuthor().equals(user) && e.getChannel().equals(channel),
							e -> {
								try {
									selectChannel(e, server);
								} catch(SQLException e1) {
									RepoResources.sqlErrorReport(channel, server, e1);
									return;
								}
							},
							2, TimeUnit.MINUTES,
							() -> endCreateChannelTime(channel));

				}else if(reactionEmote.getName().equals(UNICODE_TWO)) {

					cleanChannel = guild.getTextChannelById(guild.createTextChannel("clean-channel").complete().getId());

					cleanChannelOption = tmpCleanChannelOption;

					try {
						ConfigRepository.updateCleanChannelOption(guildId, cleanChannel.getIdLong(), cleanChannelOption.toString());
					} catch(SQLException e) {
						RepoResources.sqlErrorReport(cleanChannel, server, e);
						return;
					}

					if(cleanChannelOption.equals(CleanChannelOptionInfo.ONLY_YUUMI_COMMANDS)) {
						cleanChannel.getManager().setTopic(LanguageManager.getText(server.serv_language,
								"cleanChannelOptionTopicChannelYuumiCommands")).queue();
					}else {
						cleanChannel.getManager().setTopic(LanguageManager.getText(server.serv_language, "cleanChannelOptionTopicChannelAll")).queue();
					}

					channel.sendMessage(LanguageManager.getText(server.serv_language, "cleanChannelOptionCreatedDoneMessage")).queue();

				}
			}
		};
	}

	private void selectChannel(MessageReceivedEvent event, DTO.Server server) throws SQLException {
		event.getTextChannel().sendTyping().complete();

		List<TextChannel> textsChannel = event.getMessage().getMentionedChannels();

		if(textsChannel.size() == 1) {
			TextChannel textChannel = textsChannel.get(0);
			DTO.InfoChannel infochannel = InfoChannelRepository.getInfoChannel(server.serv_guildId);
			if(infochannel == null || infochannel.infochannel_channelid != textChannel.getIdLong()) {

				cleanChannel = textChannel;
				cleanChannelOption = tmpCleanChannelOption;

				ConfigRepository.updateCleanChannelOption(guildId, cleanChannel.getIdLong(), cleanChannelOption.toString());

				if(cleanChannelOption.equals(CleanChannelOptionInfo.ONLY_YUUMI_COMMANDS)) {
					textChannel.sendMessage(LanguageManager.getText(server.serv_language, "cleanChannelOptionInfoMessageYuumiCommands")).complete();
				}else {
					textChannel.sendMessage(LanguageManager.getText(server.serv_language, "cleanChannelOptionInfoMessageAll")).complete();
				}

				event.getTextChannel().sendMessage(LanguageManager.getText(server.serv_language, "cleanChannelOptionSettedDoneMessage")).queue();

			}else {
				event.getTextChannel().sendMessage(LanguageManager.getText(server.serv_language, "cleanChannelOptionSelectInfoChannel")).queue();
			}
		}else {
			event.getTextChannel().sendMessage(LanguageManager.getText(server.serv_language, "cleanChannelOptionOneTextChannelRequired")).queue();
		}
	}

	private void endCreateChannelTime(MessageChannel channel) {
		TextChannel textChannel = Yuumi.getJda().getTextChannelById(channel.getId());

		String langage = LanguageManager.DEFAULT_LANGUAGE;
		if(textChannel != null) {
			DTO.Server server = null;
			try {
				server = ServerRepository.getServerWithGuildId(textChannel.getGuild().getIdLong());
			} catch(SQLException e) {
				logger.error("SQL Error when getting the server in CleanChannelOption setup !", e);
			}
			if(server != null) {
				langage = server.serv_language;
			}
		}
		channel.sendMessage(LanguageManager.getText(langage, "cleanChannelOptionResponseTimeOut")).queue();
	}

	private Consumer<Message> finalAction(){
		return new Consumer<Message>() {

			@Override
			public void accept(Message message) {
				message.clearReactions().complete();
			}};
	}

	@Override
	public String getChoiceText(String langage) throws SQLException {

		if(cleanChannel != null && Yuumi.getJda().getTextChannelById(cleanChannel.getId()) == null) {
			cleanChannel = null;
			cleanChannelOption = CleanChannelOptionInfo.DISABLE;
			ConfigRepository.updateCleanChannelOption(guildId, 0, cleanChannelOption.toString());
		}

		String status = LanguageManager.getText(langage, "optionDisable");

		if(cleanChannel != null) {
			status = String.format(LanguageManager.getText(langage, "cleanChannelOptionEnable"),
					LanguageManager.getText(langage, cleanChannelOption.name), cleanChannel.getAsMention());
		}else {
			cleanChannelOption = CleanChannelOptionInfo.DISABLE;
		}

		return LanguageManager.getText(langage, description) + " : " + status;
	}

	public CleanChannelOptionInfo getCleanChannelOption() {
		return cleanChannelOption;
	}

	public TextChannel getCleanChannel() {
		return cleanChannel;
	}

	public void setCleanChannelOption(CleanChannelOptionInfo cleanChannelOption) {
		this.cleanChannelOption = cleanChannelOption;
	}

	public void setCleanChannel(TextChannel cleanChannel) {
		this.cleanChannel = cleanChannel;
	}

}
