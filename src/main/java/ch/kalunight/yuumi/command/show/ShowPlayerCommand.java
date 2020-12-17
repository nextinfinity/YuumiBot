package ch.kalunight.yuumi.command.show;

import java.awt.Color;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import ch.kalunight.yuumi.Yuumi;
import ch.kalunight.yuumi.command.YuumiCommand;
import ch.kalunight.yuumi.model.dto.DTO;
import ch.kalunight.yuumi.repositories.LeagueAccountRepository;
import ch.kalunight.yuumi.repositories.PlayerRepository;
import ch.kalunight.yuumi.translation.LanguageManager;
import ch.kalunight.yuumi.util.CommandUtil;
import ch.kalunight.yuumi.util.RiotApiUtil;
import ch.kalunight.yuumi.util.request.RiotRequest;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;

public class ShowPlayerCommand extends YuumiCommand {

	public static final String USAGE_NAME = "players";

	private final EventWaiter waiter;

	public ShowPlayerCommand(EventWaiter eventWaiter) {
		this.name = USAGE_NAME;
		String[] aliases = {"p", "player"};
		this.arguments = "";
		this.aliases = aliases;
		this.waiter = eventWaiter;
		this.help = "showPlayerHelpMessage";
		this.cooldown = 10;
		this.helpBiConsumer = CommandUtil.getHelpMethodIsChildren(ShowCommand.USAGE_NAME, name, arguments, help);
		Permission[] botPermissionNeeded = {Permission.MANAGE_EMOTES, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_MANAGE};
		this.botPermissions = botPermissionNeeded;
	}

	@Override
	protected void executeCommand(CommandEvent event) throws SQLException {
		DTO.Server server = getServer(event.getGuild().getIdLong());

		Paginator.Builder pbuilder = new Paginator.Builder()
				.setColumns(1)
				.setItemsPerPage(5)
				.showPageNumbers(true)
				.waitOnSinglePage(false)
				.useNumberedItems(true)
				.setFinalAction(m -> {
					try {
						m.clearReactions().queue();
					} catch(PermissionException ex) {
						m.delete().queue();
					}
				})
				.setEventWaiter(waiter)
				.setTimeout(1, TimeUnit.MINUTES);

		int page = 1;

		List<DTO.Player> players = PlayerRepository.getPlayers(server.serv_guildId);

		if(players.isEmpty()) {
			event.reply(LanguageManager.getText(server.serv_language, "showPlayerServerEmpty"));
			return;
		}

		int accountsNmb = 0;
		for(DTO.Player player : players) {
			StringBuilder playerInfo = new StringBuilder();
			User user = event.getGuild().retrieveMemberById(player.player_discordId).complete().getUser();
			playerInfo.append(String.format(LanguageManager.getText(server.serv_language, "showPlayerName"),
					user.getName()) + "\n");

			List<DTO.LeagueAccount> leagueAccounts = LeagueAccountRepository.getLeaguesAccounts(server.serv_guildId, user.getIdLong());

			if(leagueAccounts.isEmpty()) {
				playerInfo.append(LanguageManager.getText(server.serv_language, "showPlayerNoAccount") + "\n");
			}
			accountsNmb += leagueAccounts.size();
			for(DTO.LeagueAccount leagueAccount : leagueAccounts) {
				Summoner summoner;
				try {
					summoner = Yuumi.getRiotApi().getSummoner(leagueAccount.leagueAccount_server,
							leagueAccount.leagueAccount_summonerId);
				} catch(RiotApiException e) {
					RiotApiUtil.handleRiotApi(event.getEvent(), e, server.serv_language);
					return;
				}
				playerInfo.append(String.format(LanguageManager.getText(server.serv_language, "showPlayerAccount"),
						summoner.getName(), leagueAccount.leagueAccount_server.getName().toUpperCase(),
						RiotRequest.getSoloqRank(leagueAccount.leagueAccount_summonerId,
								leagueAccount.leagueAccount_server).toString(server.serv_language)) + "\n");
			}
			pbuilder.addItems(playerInfo.toString().substring(0, playerInfo.toString().length() - 1));
		}

		Paginator p = pbuilder.setColor(Color.GREEN)
				.setText(String.format(LanguageManager.getText(server.serv_language, "showPlayerEmbedTitle"), players.size(), accountsNmb))
				.setUsers(event.getAuthor())
				.build();
		p.paginate(event.getChannel(), page);

	}

	@Override
	public BiConsumer<CommandEvent, Command> getHelpBiConsumer(CommandEvent event) {
		return helpBiConsumer;
	}
}