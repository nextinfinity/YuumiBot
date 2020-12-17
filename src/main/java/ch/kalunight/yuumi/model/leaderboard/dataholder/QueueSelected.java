package ch.kalunight.yuumi.model.leaderboard.dataholder;

import ch.kalunight.yuumi.model.GameQueueConfigId;

public class QueueSelected {

	private int queueId;

	public QueueSelected(GameQueueConfigId gameQueue) {
		this.queueId = gameQueue.getId();
	}

	public GameQueueConfigId getGameQueue() {
		return GameQueueConfigId.getGameQueueIdWithId(queueId);
	}

	public int getQueueId() {
		return queueId;
	}

	public void setQueueId(int queueId) {
		this.queueId = queueId;
	}

}
