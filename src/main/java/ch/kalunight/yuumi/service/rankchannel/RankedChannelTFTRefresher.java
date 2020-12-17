package ch.kalunight.yuumi.service.rankchannel;

import ch.kalunight.yuumi.Yuumi;
import ch.kalunight.yuumi.exception.NoValueRankException;
import ch.kalunight.yuumi.model.dto.DTO.LeagueAccount;
import ch.kalunight.yuumi.model.dto.DTO.Player;
import ch.kalunight.yuumi.model.dto.DTO.RankHistoryChannel;
import ch.kalunight.yuumi.model.dto.DTO.Server;
import ch.kalunight.yuumi.util.request.MessageBuilderRequest;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.rithms.riot.api.endpoints.league.dto.LeagueEntry;
import net.rithms.riot.api.endpoints.tft_match.dto.TFTMatch;

public class RankedChannelTFTRefresher extends RankedChannelBaseRefresher {

	private TFTMatch match;

	public RankedChannelTFTRefresher(RankHistoryChannel rankChannel, LeagueEntry oldEntry, LeagueEntry newEntry, Player player,
									 LeagueAccount leagueAccount, Server server, TFTMatch match) {
		super(rankChannel, oldEntry, newEntry, player, leagueAccount, server);
		this.match = match;
	}

	@Override
	protected void sendRankChangedWithoutBO() {
		sendTFTStandardMessage();
	}

	@Override
	protected void sendLeaguePointChangeOnly() {
		sendTFTStandardMessage();
	}

	private void sendTFTStandardMessage() {

		MessageEmbed message;
		try {
			message = MessageBuilderRequest.createRankChannelCardLeaguePointChangeOnlyTFT
					(oldEntry, newEntry, match, player, leagueAccount, server.serv_language);
		} catch (NoValueRankException e) {
			logger.warn("Error while generating a TFT rank message!", e);
			return;
		}

		TextChannel textChannelWhereSend = Yuumi.getJda().getTextChannelById(rankChannel.rhChannel_channelId);
		if(textChannelWhereSend != null) {
			textChannelWhereSend.sendMessage(message).queue();
		}
	}

	@Override
	protected void sendBOEnded() {
		//TFT doesn't handle this event
	}

	@Override
	protected void sendBOStarted() {
		//TFT doesn't handle this event
	}

	@Override
	protected void sendBOInProgess() {
		//TFT doesn't handle this event
	}

}
