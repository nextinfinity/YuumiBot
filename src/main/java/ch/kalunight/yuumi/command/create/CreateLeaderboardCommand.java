package ch.kalunight.yuumi.command.create;

import java.awt.Color;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.SelectionDialog;

import ch.kalunight.yuumi.command.YuumiCommand;
import ch.kalunight.yuumi.model.dto.DTO;
import ch.kalunight.yuumi.model.dto.DTO.Server;
import ch.kalunight.yuumi.model.leaderboard.LeaderboardExtraDataHandler;
import ch.kalunight.yuumi.model.leaderboard.dataholder.Objective;
import ch.kalunight.yuumi.translation.LanguageManager;
import ch.kalunight.yuumi.util.CommandUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

public class CreateLeaderboardCommand extends YuumiCommand {

	private EventWaiter waiter;

	public CreateLeaderboardCommand(EventWaiter waiter) {
		this.name = "leaderboard";
		String[] aliases = {"leader", "lb", "lead", "board"};
		this.aliases = aliases;
		this.arguments = "";
		Permission[] permissionRequired = {Permission.MANAGE_CHANNEL};
		this.userPermissions = permissionRequired;
		this.guildOnly = true;
		this.help = "createLeaderboardHelpMessage";
		this.waiter = waiter;
		this.helpBiConsumer = CommandUtil.getHelpMethodIsChildren(CreateCommand.USAGE_NAME, name, arguments, help);
	}

	@Override
	protected void executeCommand(CommandEvent event) throws SQLException {

		DTO.Server server = getServer(event.getGuild().getIdLong());

		event.reply(LanguageManager.getText(server.serv_language, "createLeaderboardExplainMessage"));

		List<Objective> objectiveList = new ArrayList<>();
		List<String> objectiveChoices = new ArrayList<>();
		AtomicBoolean actionDone = new AtomicBoolean(false);

		SelectionDialog.Builder selectAccountBuilder = new SelectionDialog.Builder()
				.addUsers(event.getAuthor())
				.setEventWaiter(waiter)
				.useLooping(true)
				.setColor(Color.GREEN)
				.setSelectedEnds("**", "**")
				.setCanceled(getSelectionCancelAction(server.serv_language, actionDone))
				.setSelectionConsumer(getSelectionConsumer(server, event, objectiveList, actionDone))
				.setTimeout(2, TimeUnit.MINUTES);

		for(Objective objective : Objective.values()) {
			String actualChoice = String.format(LanguageManager.getText(server.serv_language, objective.getTranslationId()));

			objectiveChoices.add(actualChoice);
			selectAccountBuilder.addChoices(actualChoice);
			objectiveList.add(objective);
		}

		selectAccountBuilder.setText(LanguageManager.getText(server.serv_language, "createLeaderboardTitleListObjective"));

		SelectionDialog choiceLeaderBoard = selectAccountBuilder.build();
		choiceLeaderBoard.display(event.getChannel());
	}

	private Consumer<Message> getSelectionCancelAction(String language, AtomicBoolean selectionDone){
		return new Consumer<Message>() {
			@Override
			public void accept(Message message) {
				if(!selectionDone.get()) {
					message.clearReactions().queue();
					message.editMessage(LanguageManager.getText(language, "createLeaderboardCancelMessage")).queue();
				}
			}
		};
	}

	private BiConsumer<Message, Integer> getSelectionConsumer(Server server, CommandEvent event, List<Objective> objectiveList, AtomicBoolean selectionDone) {
		return new BiConsumer<Message, Integer>() {
			@Override
			public void accept(Message selectionMessage, Integer objectiveSelection) {
				selectionMessage.clearReactions().queue();
				selectionDone.set(true);

				Objective objective = objectiveList.get(objectiveSelection - 1);

				event.reply(String.format(LanguageManager.getText(server.serv_language, "leaderboardObjectiveSelected"),
						LanguageManager.getText(server.serv_language, objective.getTranslationId())));

				LeaderboardExtraDataHandler dataNeeded = Objective.getDataNeeded(objective, waiter, server, event);

				dataNeeded.handleSecondCreationPart();
			}
		};
	}

	@Override
	public BiConsumer<CommandEvent, Command> getHelpBiConsumer(CommandEvent event) {
		return helpBiConsumer;
	}

}
