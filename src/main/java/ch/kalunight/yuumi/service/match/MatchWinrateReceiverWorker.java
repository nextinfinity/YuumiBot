package ch.kalunight.yuumi.service.match;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.kalunight.yuumi.model.WinRateReceiver;
import ch.kalunight.yuumi.model.dto.SavedMatch;
import ch.kalunight.yuumi.model.dto.DTO.MatchCache;
import ch.kalunight.yuumi.riotapi.CacheManager;
import net.rithms.riot.api.endpoints.match.dto.Match;
import net.rithms.riot.api.endpoints.match.dto.MatchReference;
import net.rithms.riot.api.endpoints.match.dto.Participant;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;

public class MatchWinrateReceiverWorker extends MatchReceiverWorker {

	private WinRateReceiver winRateReceiver;

	public MatchWinrateReceiverWorker(WinRateReceiver winRateReceiver, AtomicBoolean gameLoadingConflict,
									  MatchReference matchReference, Platform server, Summoner summoner) {
		super(gameLoadingConflict, matchReference, server, summoner);
		this.winRateReceiver = winRateReceiver;
	}

	@Override
	public void runMatchReceveirWorker(MatchCache matchCache) {
		try {
			if(matchCache != null) {
				SavedMatch cacheMatch = matchCache.mCatch_savedMatch;

				if(cacheMatch.isGivenAccountWinner(summoner.getAccountId())) {
					winRateReceiver.win.incrementAndGet();
				}else {
					winRateReceiver.loose.incrementAndGet();
				}
			}else {
				Match match = riotApi.getMatchWithRateLimit(server, matchReference.getGameId());

				if(match == null) {
					return;
				}

				Participant participant = match.getParticipantByAccountId(summoner.getAccountId());

				if(participant != null && participant.getTimeline().getCreepsPerMinDeltas() != null) { // Check if the game has been canceled

					String result = match.getTeamByTeamId(participant.getTeamId()).getWin();
					if(result.equalsIgnoreCase("Fail")) {
						winRateReceiver.loose.incrementAndGet();
						CacheManager.createCacheMatch(server, match);
					}

					if(result.equalsIgnoreCase("Win")) {
						winRateReceiver.win.incrementAndGet();
						CacheManager.createCacheMatch(server, match);
					}
				}
			}

		}catch(SQLException e) {
			logger.info("SQL error (unique constraint error, normaly nothing severe) Error : {}", e.getMessage());
			gameLoadingConflict.set(true);
		}
	}
}
