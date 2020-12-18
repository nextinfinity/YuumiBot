package ch.kalunight.yuumi.command.create;

import java.sql.SQLException;
import java.util.function.BiConsumer;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import ch.kalunight.yuumi.command.YuumiCommand;
import ch.kalunight.yuumi.model.dto.DTO;
import ch.kalunight.yuumi.repositories.TeamRepository;
import ch.kalunight.yuumi.translation.LanguageManager;
import ch.kalunight.yuumi.util.CommandUtil;
import net.dv8tion.jda.api.Permission;

public class CreateTeamCommand extends YuumiCommand {

	public static final String USAGE_NAME = "team";

	public CreateTeamCommand() {
		this.name = USAGE_NAME;
		this.arguments = "nameOfTheTeam";
		Permission[] permissionRequired = {Permission.MANAGE_CHANNEL};
		this.userPermissions = permissionRequired;
		this.help = "createTeamHelpMessage";
		this.helpBiConsumer = CommandUtil.getHelpMethodIsChildren(CreateCommand.USAGE_NAME, name, arguments, help);
	}

	@Override
	protected void executeCommand(CommandEvent event) throws SQLException {

		event.getTextChannel().sendTyping().complete();

		DTO.Server server = getServer(event.getGuild().getIdLong());

		String nameTeam = event.getArgs();

		if(!checkNameValid(nameTeam)) {
			event.reply(LanguageManager.getText(server.serv_language, "nameUseIllegalCharacter"));
			return;
		}

		if(nameTeam.equals("--server")) {
			event.reply(LanguageManager.getText(server.serv_language, "nameAlreadyUsedByTheSystem"));
			return;
		}

		if(nameTeam.equals("")) {
			event.reply(LanguageManager.getText(server.serv_language, "createTeamNeedName"));
		} else {
			DTO.Team team = TeamRepository.getTeam(server.serv_guildId, nameTeam);

			if(team != null) {
				event.reply(LanguageManager.getText(server.serv_language, "createTeamNameAlreadyExist"));
			} else {
				TeamRepository.createTeam(server.serv_id, nameTeam);
				event.reply(String.format(LanguageManager.getText(server.serv_language, "createTeamDoneMessage"), event.getArgs()));
			}
		}
	}

	private boolean checkNameValid(String nameToCheck) {

		boolean nameInvalid = false;

		nameInvalid = nameToCheck.contains("*");
		if(nameInvalid) {
			return false;
		}

		nameInvalid = nameToCheck.contains("_");
		if(nameInvalid) {
			return false;
		}

		nameInvalid = nameToCheck.contains(">");
		if(nameInvalid) {
			return false;
		}

		nameInvalid = nameToCheck.contains("`");
		if(nameInvalid) {
			return false;
		}else {
			return true;
		}
	}

	@Override
	public BiConsumer<CommandEvent, Command> getHelpBiConsumer(CommandEvent event) {
		return helpBiConsumer;
	}
}