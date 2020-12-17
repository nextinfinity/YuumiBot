package ch.kalunight.yuumi.model.leaderboard.dataholder;

import ch.kalunight.yuumi.model.GameQueueConfigId;
import ch.kalunight.yuumi.model.dto.DTO;
import ch.kalunight.yuumi.model.player_data.FullTier;

public class PlayerRank implements Comparable<PlayerRank>{

	private DTO.Player player;
	private FullTier fullTier;
	private GameQueueConfigId queue;

	public PlayerRank(DTO.Player player, FullTier fullTier, GameQueueConfigId queue) {
		this.player = player;
		this.fullTier = fullTier;
		this.queue = queue;
	}

	@Override
	public int compareTo(PlayerRank otherPlayer) {
		return fullTier.compareTo(otherPlayer.fullTier);
	}

	public DTO.Player getPlayer() {
		return player;
	}

	public FullTier getFullTier() {
		return fullTier;
	}

	public GameQueueConfigId getQueue() {
		return queue;
	}
}
