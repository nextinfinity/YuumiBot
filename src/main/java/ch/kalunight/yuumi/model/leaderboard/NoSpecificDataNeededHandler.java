package ch.kalunight.yuumi.model.leaderboard;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import ch.kalunight.yuumi.model.dto.DTO.Server;
import ch.kalunight.yuumi.model.leaderboard.dataholder.Objective;
import ch.kalunight.yuumi.translation.LanguageManager;

public class NoSpecificDataNeededHandler extends LeaderboardExtraDataHandler {

	public NoSpecificDataNeededHandler(Objective objective, EventWaiter waiter, CommandEvent event, Server server) {
		super(objective, waiter, event, server);
	}

	@Override
	public void handleSecondCreationPart() {
		event.getTextChannel().sendMessage(LanguageManager.getText(server.serv_language, "leaderboardSelectChannelLeaderboard")).queue();
		handleEndOfCreation("");
	}

}
