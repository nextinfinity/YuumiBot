package ch.kalunight.yuumi.command.remove;

import java.sql.SQLException;
import java.util.function.BiConsumer;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import ch.kalunight.yuumi.Yuumi;
import ch.kalunight.yuumi.command.YuumiCommand;
import ch.kalunight.yuumi.model.dto.DTO;
import ch.kalunight.yuumi.repositories.PlayerRepository;
import ch.kalunight.yuumi.repositories.TeamRepository;
import ch.kalunight.yuumi.translation.LanguageManager;
import ch.kalunight.yuumi.util.CommandUtil;
import net.dv8tion.jda.api.Permission;

public class RemovePlayerToTeamCommand extends YuumiCommand {

	public static final String USAGE_NAME = "playerToTeam";

	public RemovePlayerToTeamCommand() {
		this.name = USAGE_NAME;
		this.help = "removePlayerToTeamHelpMessage";
		this.arguments = "@MentionOfPlayer";
		Permission[] permissionRequired = {Permission.MANAGE_CHANNEL};
		this.userPermissions = permissionRequired;
		this.helpBiConsumer = CommandUtil.getHelpMethodIsChildren(RemoveCommand.USAGE_NAME, name, arguments, help);
	}

	@Override
	protected void executeCommand(CommandEvent event) throws SQLException {
		event.getTextChannel().sendTyping().complete();

		DTO.Server server = getServer(event.getGuild().getIdLong());

		if(event.getMessage().getMentionedMembers().size() != 1) {
			event.reply(LanguageManager.getText(server.serv_language, "removePlayerToTeamMissingMention"));
			return;
		}

		DTO.Player player = PlayerRepository.getPlayer(server.serv_guildId, event.getMessage()
				.getMentionedMembers().get(0).getUser().getIdLong());

		if(player == null) {
			event.reply(LanguageManager.getText(server.serv_language, "removePlayerToTeamMentionnedPlayerNotPlayer"));
			return;
		}

		DTO.Team teamWhereRemove = TeamRepository.getTeamByPlayerAndGuild(server.serv_guildId, player.player_discordId);

		if(teamWhereRemove == null) {
			event.reply(LanguageManager.getText(server.serv_language, "removePlayerToTeamNotInTheTeam"));
			return;
		}

		PlayerRepository.updateTeamOfPlayerDefineNull(player.player_id);
		event.reply(String.format(LanguageManager.getText(server.serv_language, "removePlayerToTeamDoneMessage"),
				Yuumi.getJda().retrieveUserById(player.player_discordId).complete().getName(), teamWhereRemove.team_name));
	}

	@Override
	public BiConsumer<CommandEvent, Command> getHelpBiConsumer(CommandEvent event) {
		return helpBiConsumer;
	}
}
