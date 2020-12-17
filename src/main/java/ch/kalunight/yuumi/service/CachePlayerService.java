package ch.kalunight.yuumi.service;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.kalunight.yuumi.Yuumi;
import ch.kalunight.yuumi.repositories.PlayerRepository;
import ch.kalunight.yuumi.util.Resources;
import net.dv8tion.jda.api.entities.Guild;

public class CachePlayerService implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(CachePlayerService.class);

	@Override
	public void run() {
		try {
			Set<Entry<Long, List<Long>>> listEntry = PlayerRepository.getListDiscordIdOfRegisteredPlayers().entrySet();

			int count = 0;
			for(Entry<Long, List<Long>> oneGuild : listEntry) {
				count++;
				if(!Resources.getBlackListedServer().contains(oneGuild.getKey())) {
					logger.info("Load server guild {}/{}", count, listEntry.size());
					Guild guild = Yuumi.getJda().getGuildById(oneGuild.getKey());
					if(guild != null) {
						guild.findMembers(e -> PlayerRepository.getListDiscordIdOfRegisteredPlayers().get(oneGuild.getKey()).contains(e.getIdLong()));
					}
				}
			}

			logger.info("Load of all guild Ended !");

		} catch (Exception e) {
			logger.error("Error while loading all players !", e);
		}
	}
}
