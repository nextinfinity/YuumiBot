package ch.kalunight.yuumi.model.leaderboard.dataholder;

import ch.kalunight.yuumi.exception.NoValueRankException;
import ch.kalunight.yuumi.model.dto.DTO.LeagueAccount;
import ch.kalunight.yuumi.model.player_data.FullTier;

public class PlayerProgression {

	private String playerSummonerId;
	private String region;
	private int startingElo;

	public PlayerProgression(LeagueAccount leagueAccount, FullTier fullTier) throws NoValueRankException {
		this.playerSummonerId = leagueAccount.leagueAccount_summonerId;
		this.region = leagueAccount.leagueAccount_server.getId();
		this.startingElo = fullTier.value();
	}

	public String getPlayerSummonerId() {
		return playerSummonerId;
	}

	public String getRegion() {
		return region;
	}

	public int getStartingElo() {
		return startingElo;
	}
}
