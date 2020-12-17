package ch.kalunight.yuumi.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ch.kalunight.yuumi.Yuumi;
import ch.kalunight.yuumi.model.GameQueueConfigId;
import ch.kalunight.yuumi.model.dto.DTO.LastRank;
import ch.kalunight.yuumi.model.dto.DTO.LeagueAccount;
import ch.kalunight.yuumi.repositories.LastRankRepository;
import net.rithms.riot.api.endpoints.tft_match.dto.TFTMatch;
import net.rithms.riot.constant.Platform;

public class TFTMatchUtil {

	private TFTMatchUtil() {
		// Hide default public constructor
	}

	public static List<TFTMatch> getTFTRankedMatchsSinceTheLastMessage(LeagueAccount leagueAccount, LastRank lastRank) throws SQLException{
		List<TFTMatch> matchs = new ArrayList<>();

		List<String> tftMatchsList = Yuumi.getRiotApi()
				.getTFTMatchListWithRateLimit(leagueAccount.leagueAccount_server, leagueAccount.leagueAccount_tftPuuid, 20);

		for(String matchId : tftMatchsList) {

			TFTMatch match = Yuumi.getRiotApi().getTFTMatchWithRateLimit(leagueAccount.leagueAccount_server, matchId);

			if(match.getInfo().getQueueId() == GameQueueConfigId.RANKED_TFT.getId()) {
				matchs.add(match);

				if(lastRank.lastRank_tftLastTreatedMatchId == null) {
					LastRankRepository.updateLastRankTFTLastTreatedMatch(match.getMetadata().getMatchId(), lastRank);
					matchs.clear();
					return matchs;
				}

				if(lastRank.lastRank_tftLastTreatedMatchId.equals(matchId)) {
					break;
				}
			}
		}

		if(lastRank.lastRank_tftLastTreatedMatchId != null) {
			matchs = getMatchsAfterLastGame(matchs, lastRank.lastRank_tftLastTreatedMatchId, leagueAccount.leagueAccount_server);
		}

		if(!matchs.isEmpty()) {

			TFTMatch lastRankedMatch = getLatestMatch(matchs);

			if(lastRankedMatch != null) {

				LastRankRepository.updateLastRankTFTLastTreatedMatch(lastRankedMatch.getMetadata().getMatchId(), lastRank);
			}
		}
		return matchs;
	}

	private static List<TFTMatch> getMatchsAfterLastGame(List<TFTMatch> matchs, String lastTreatedGameID, Platform platform) {

		List<TFTMatch> matchsAfterTheGame = new ArrayList<>();

		TFTMatch lastTreatedMatch = null;
		for(TFTMatch match : matchs) {
			if(match.getMetadata().getMatchId().equals(lastTreatedGameID)) {
				lastTreatedMatch = match;
				break;
			}
		}

		if(lastTreatedMatch == null) {
			return matchs;
		}

		for(TFTMatch match : matchs) {
			if(lastTreatedMatch.getInfo().getGameDateTime() < match.getInfo().getGameDateTime()) {
				matchsAfterTheGame.add(match);
			}
		}
		return matchsAfterTheGame;
	}

	private static TFTMatch getLatestMatch(List<TFTMatch> matchs) {
		TFTMatch lastRankedMatch = null;

		for(TFTMatch match : matchs) {
			if(lastRankedMatch == null || lastRankedMatch.getInfo().getGameDateTime() < match.getInfo().getGameDateTime()) {
				lastRankedMatch = match;
			}
		}
		return lastRankedMatch;
	}

}
