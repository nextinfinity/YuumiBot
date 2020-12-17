package ch.kalunight.yuumi.model.leaderboard.dataholder;

import ch.kalunight.yuumi.model.static_data.Champion;
import ch.kalunight.yuumi.util.Resources;

public class SpecificChamp {
	private int championKey;

	public SpecificChamp(Champion champion) {
		this.championKey = champion.getKey();
	}

	public Champion getChampion() {
		return Resources.getChampionDataById(championKey);
	}

	public void setChampion(Champion champion) {
		this.championKey = champion.getKey();
	}
}
