package ch.kalunight.yuumi.model.config;

import java.util.ArrayList;
import java.util.List;
import ch.kalunight.yuumi.model.config.option.ConfigurationOption;
import ch.kalunight.yuumi.model.config.option.GameInfoCardOption;
import ch.kalunight.yuumi.model.config.option.InfoPanelRankedOption;
import ch.kalunight.yuumi.model.config.option.RegionOption;
import ch.kalunight.yuumi.model.config.option.CleanChannelOption;
import ch.kalunight.yuumi.model.config.option.RoleOption;
import ch.kalunight.yuumi.model.config.option.SelfAddingOption;

public class ServerConfiguration {

	/**
	 * Define if user can add them self in the system.
	 */
	private SelfAddingOption userSelfAdding;

	/**
	 * Default region of the server. If a account/player is created without the region and this option is activated the default region will be used.
	 */
	private RegionOption defaultRegion;

	/**
	 * This option hide channels of Yuumi to non-register players.
	 */
	private RoleOption yuumiRoleOption;

	/**
	 * This option activate the system of automatically send infocards in infochannel. Enable as default.
	 */
	private GameInfoCardOption infoCardsOption;

	/**
	 * This option let config a "Clean Channel"
	 */
	private CleanChannelOption cleanChannelOption;

	/**
	 * This option let config the data of infopanel
	 */
	private InfoPanelRankedOption infopanelRankedOption;

	/**
	 * This option activate the command join/leave for everyone. They can join team joinable by everyone. NOT IMPLEMENTED
	 */
	private boolean everyoneCanMoveOfTeam = false;

	public ServerConfiguration(long guildId) {
		this.defaultRegion = new RegionOption(guildId);
		this.yuumiRoleOption = new RoleOption(guildId);
		this.userSelfAdding = new SelfAddingOption(guildId);
		this.infoCardsOption = new GameInfoCardOption(guildId);
		this.cleanChannelOption = new CleanChannelOption(guildId);
		this.infopanelRankedOption = new InfoPanelRankedOption(guildId);
		this.everyoneCanMoveOfTeam = false;
	}

	public List<ConfigurationOption> getAllConfigurationOption() {
		List<ConfigurationOption> options = new ArrayList<>();
		options.add(defaultRegion);
		options.add(yuumiRoleOption);
		options.add(cleanChannelOption);
		options.add(userSelfAdding);
		options.add(infoCardsOption);
		options.add(infopanelRankedOption);
		return options;
	}

	public RegionOption getDefaultRegion() {
		return defaultRegion;
	}

	public RoleOption getYuumiRoleOption() {
		return yuumiRoleOption;
	}

	public boolean isEveryoneCanMoveOfTeam() {
		return everyoneCanMoveOfTeam;
	}

	public SelfAddingOption getUserSelfAdding() {
		return userSelfAdding;
	}

	public GameInfoCardOption getInfoCardsOption() {
		return infoCardsOption;
	}

	public CleanChannelOption getCleanChannelOption() {
		return cleanChannelOption;
	}

	public void setUserSelfAdding(SelfAddingOption userSelfAdding) {
		this.userSelfAdding = userSelfAdding;
	}

	public void setDefaultRegion(RegionOption defaultRegion) {
		this.defaultRegion = defaultRegion;
	}

	public void setYuumiRoleOption(RoleOption yuumiRoleOption) {
		this.yuumiRoleOption = yuumiRoleOption;
	}

	public void setInfoCardsOption(GameInfoCardOption infoCardsOption) {
		this.infoCardsOption = infoCardsOption;
	}

	public void setCleanChannelOption(CleanChannelOption cleanChannelOption) {
		this.cleanChannelOption = cleanChannelOption;
	}

	public InfoPanelRankedOption getInfopanelRankedOption() {
		return infopanelRankedOption;
	}

	public void setInfopanelRankedOption(InfoPanelRankedOption infopanelRankedOption) {
		this.infopanelRankedOption = infopanelRankedOption;
	}
}
