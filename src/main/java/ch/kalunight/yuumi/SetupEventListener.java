package ch.kalunight.yuumi;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.discordbots.api.client.DiscordBotListAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import ch.kalunight.yuumi.repositories.ServerRepository;
import ch.kalunight.yuumi.repositories.ServerStatusRepository;
import ch.kalunight.yuumi.riotapi.CacheManager;
import ch.kalunight.yuumi.service.CachePlayerService;
import ch.kalunight.yuumi.service.RiotApiUsageChannelRefresh;
import ch.kalunight.yuumi.service.ServerChecker;
import ch.kalunight.yuumi.translation.LanguageManager;
import ch.kalunight.yuumi.util.EventListenerUtil;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SetupEventListener extends ListenerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(SetupEventListener.class);

	private static boolean yuumiIsBooted = false;

	@Override
	public void onReady(ReadyEvent event) {
		Yuumi.getJda().getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
		Yuumi.getJda().getPresence().setActivity(Activity.playing("Booting ..."));

		logger.info("Loading translations ...");
		try {
			LanguageManager.loadTranslations();
		} catch(IOException e) {
			logger.error("Critical error with the loading of translations (File issue) !", e);
			System.exit(1);
		}
		logger.info("Successfully loaded translations!");

		logger.info("Setup non initialized Guild ...");
		try {
			setupNonInitializedGuild();
		} catch(SQLException e) {
			logger.error("Issue when setup non initialized Guild !", e);
			System.exit(1);
		}
		logger.info("Setup non initialized Guild Done !");

		logger.info("Loading of champions ...");
		try {
			Yuumi.loadChampions();
		} catch(IOException e) {
			logger.error("Critical error with the loading of champions !", e);
			System.exit(1);
		}

		logger.info("Loading of champions finished !");

		logger.info("Loading of emotes ...");
		try {
			EventListenerUtil.loadCustomEmotes();
			logger.info("Loading of emotes finished !");
		} catch(IOException e) {
			logger.warn("Error with the loading of emotes : {}", e.getMessage());
		}

		logger.info("Setup cache ...");
		CacheManager.setupCache();
		logger.info("Setup cache finished !");

		logger.info("Loading of RAPI Status Channel ...");

		initRAPIStatusChannel();

		logger.info("Loading of RAPI Status Channel finished !");

		logger.info("Loading of DiscordBotList API ...");

		try {
			Yuumi.setBotListApi(new DiscordBotListAPI.Builder().botId(Yuumi.getJda().getSelfUser().getId())
					.token(Yuumi.getDiscordBotListToken()).build());

			logger.info("Loading of DiscordBotList API finished !");
		} catch(Exception e) {
			logger.info("Discord bot list api not loaded normally ! Working of the bot not affected");
			Yuumi.setBotListApi(null);
		}

		logger.info("Setup of main thread  ...");
		setupContinousRefreshThread();
		logger.info("Setup of main thread finished !");

		logger.info("Setup of commands ...");
		EventWaiter eventWaiter = new EventWaiter(ServerData.getResponseWaiter(), false);

		for(Command command : Yuumi.getMainCommands(eventWaiter)) {
			Yuumi.getCommandClient().addCommand(command);
		}
		Yuumi.getEventlistenerlist().add(eventWaiter);
		Yuumi.getJda().addEventListener(eventWaiter);
		Yuumi.setEventWaiter(eventWaiter);
		logger.info("Setup of commands done !");

		logger.info("Setup of EventListener ...");
		EventListener eventListener = new EventListener();
		Yuumi.getEventlistenerlist().add(eventListener);
		Yuumi.getJda().addEventListener(eventListener);
		logger.info("Setup of EventListener done !");

		Yuumi.getJda().getPresence().setStatus(OnlineStatus.ONLINE);
		Yuumi.getJda().getPresence().setActivity(Activity.playing("type \">help\""));

		setYuumiIsBooted(true);

		logger.info("Cache all registered players ...");
		ServerData.getServerExecutor().execute(new CachePlayerService());
		logger.info("Cache all registered players !");

		logger.info("Booting finished !");
	}

	private void setupNonInitializedGuild() throws SQLException {
		for(Guild guild : Yuumi.getJda().getGuilds()) {
			if(!guild.getOwnerId().equals(Yuumi.getJda().getSelfUser().getId()) && !ServerRepository.checkServerExist(guild.getIdLong())) {
				ServerRepository.createNewServer(guild.getIdLong(), LanguageManager.DEFAULT_LANGUAGE);
			}
		}
		ServerStatusRepository.updateAllServerInTreatment(false);
	}

	private void setupContinousRefreshThread() {
		TimerTask mainThread = new ServerChecker();
		ServerData.getServerCheckerThreadTimer().schedule(mainThread, 10000);
	}

	private void initRAPIStatusChannel() {
		try(final BufferedReader reader = new BufferedReader(new FileReader(Yuumi.RAPI_SAVE_TXT_FILE));) {
			String line;

			List<String> args = new ArrayList<>();

			while((line = reader.readLine()) != null) {
				args.add(line);
			}

			if(args.size() == 2) {
				Guild guild = Yuumi.getJda().getGuildById(args.get(0));
				if(guild != null) {
					TextChannel rapiStatusChannel = guild.getTextChannelById(args.get(1));
					if(rapiStatusChannel != null) {
						RiotApiUsageChannelRefresh.setTextChannelId(rapiStatusChannel.getIdLong());
						RiotApiUsageChannelRefresh.setGuildId(guild.getIdLong());
						logger.info("RAPI Status channel correctly loaded.");
					}
				}
			}
		} catch(FileNotFoundException e1) {
			logger.info("Needed file doesn't exist. Will be created if needed.");
		} catch(IOException e1) {
			logger.warn("Error when loading the file of RAPI Status Channel. The older channel will be unused ! (You can re-create it)");
		}
	}

	public static boolean isYuumiBooted() {
		return yuumiIsBooted;
	}

	public static void setYuumiIsBooted(boolean yuumiIsBooted) {
		SetupEventListener.yuumiIsBooted = yuumiIsBooted;
	}


}
