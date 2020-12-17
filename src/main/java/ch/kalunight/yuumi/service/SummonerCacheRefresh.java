package ch.kalunight.yuumi.service;

import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.kalunight.yuumi.Yuumi;
import ch.kalunight.yuumi.model.dto.DTO;
import ch.kalunight.yuumi.repositories.LeagueAccountRepository;
import net.dv8tion.jda.api.entities.Guild;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;

public class SummonerCacheRefresh implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(SummonerCacheRefresh.class);

	@Override
	public void run() {
		try {
			List<Guild> guilds = Yuumi.getJda().getGuilds();
			for(Guild guild : guilds) {
				List<DTO.LeagueAccount> leagueAccounts = LeagueAccountRepository.getAllLeaguesAccounts(guild.getIdLong());

				for(DTO.LeagueAccount account : leagueAccounts) {
					refreshAccountName(account);
				}
			}
		}catch(SQLException e) {
			logger.error("SQL error when refresh the db cache !", e);
		}catch(Exception e) {
			logger.error("Unknown error when refresh the db cache !", e);
		}finally {
			logger.info("Refresh cache ended !");
		}
	}

	private void refreshAccountName(DTO.LeagueAccount account) throws SQLException {
		try {
			Summoner summoner =
					Yuumi.getRiotApi().getSummoner(account.leagueAccount_server, account.leagueAccount_summonerId);
			LeagueAccountRepository.updateAccountNameWithAccountId(account.leagueAccount_id, summoner.getName());
		}catch(RiotApiException e) {
			if(e.getErrorCode() == RiotApiException.DATA_NOT_FOUND) {
				LeagueAccountRepository.deleteAccountWithId(account.leagueAccount_id);
			}
		}
	}

}
