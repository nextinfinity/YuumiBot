package ch.kalunight.yuumi.command.add;

import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import ch.kalunight.yuumi.command.YuumiCommand;
import ch.kalunight.yuumi.model.dto.DTO;
import ch.kalunight.yuumi.repositories.PlayerRepository;
import ch.kalunight.yuumi.repositories.TeamRepository;
import ch.kalunight.yuumi.translation.LanguageManager;
import ch.kalunight.yuumi.util.CommandUtil;
import net.dv8tion.jda.api.Permission;

public class AddPlayerToTeamCommand extends YuumiCommand {

	public static final String USAGE_NAME = "playerToTeam";
	public static final Pattern PARENTHESES_PATTERN = Pattern.compile("\\(([^)]+)\\)");

	public AddPlayerToTeamCommand() {
		this.name = USAGE_NAME;
		Permission[] permissionRequired = {Permission.MANAGE_CHANNEL};
		this.arguments = "@MentionPlayer (teamName)";
		this.userPermissions = permissionRequired;
		this.help = "addPlayerToTeamCommandHelp";
		this.helpBiConsumer = CommandUtil.getHelpMethodIsChildren(AddCommand.USAGE_NAME, USAGE_NAME, arguments, help);
	}

	@Override
	protected void executeCommand(CommandEvent event) throws SQLException {
		event.getTextChannel().sendTyping().complete();
		DTO.Server server = getServer(event.getGuild().getIdLong());

		if(event.getMessage().getMentionedMembers().size() != 1) {
			event.reply(LanguageManager.getText(server.serv_language, "mentionOfPlayerNeeded"));
		} else {

			DTO.Player player = PlayerRepository.getPlayer(server.serv_guildId,
					event.getMessage().getMentionedMembers().get(0).getUser().getIdLong());

			if(player == null) {
				event.reply(LanguageManager.getText(server.serv_language, "mentionOfUserNeedToBeAPlayer"));
			} else {

				DTO.Team team = TeamRepository.getTeamByPlayerAndGuild(server.serv_guildId, player.player_discordId);
				if(team != null) {
					event.reply(String.format(LanguageManager.getText(server.serv_language, "mentionedPlayerIsAlreadyInATeam"), team.team_name));
				} else {
					Matcher matcher = PARENTHESES_PATTERN.matcher(event.getArgs());
					String teamName = "";
					while(matcher.find()) {
						teamName = matcher.group(1);
					}

					DTO.Team teamToAdd = TeamRepository.getTeam(server.serv_guildId, teamName);
					if(teamToAdd == null) {
						event.reply(LanguageManager.getText(server.serv_language, "givenTeamNotExist"));
					} else {
						PlayerRepository.updateTeamOfPlayer(player.player_id, teamToAdd.team_id);
						event.reply(LanguageManager.getText(server.serv_language, "playerAddedInTheTeam"));
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
