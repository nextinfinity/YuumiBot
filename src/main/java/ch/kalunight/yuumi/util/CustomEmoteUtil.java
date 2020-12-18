package ch.kalunight.yuumi.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ch.kalunight.yuumi.Yuumi;
import ch.kalunight.yuumi.model.player_data.Tier;
import ch.kalunight.yuumi.model.static_data.Champion;
import ch.kalunight.yuumi.model.static_data.CustomEmote;
import ch.kalunight.yuumi.model.static_data.Mastery;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;

public class CustomEmoteUtil {

	private CustomEmoteUtil() {
		// Hide default public constructor
	}

	private static final int MAX_EMOTE_BY_GUILD = 50;

	public static List<CustomEmote> loadPicturesInFile() {
		List<CustomEmote> emotes = new ArrayList<>();

		File folder = new File(Resources.FOLDER_TO_EMOTES);
		File[] listOfFiles = folder.listFiles();

		for(int i = 0; i < listOfFiles.length; i++) {
			String name = listOfFiles[i].getName();
			if(name.endsWith(".png") || name.endsWith(".jpg")) {
				name = name.substring(0, name.length() - 4);
				emotes.add(new CustomEmote(name, listOfFiles[i]));
			}
		}
		return emotes;
	}

	public static List<CustomEmote> prepareUploadOfEmotes(List<CustomEmote> customEmotes) throws IOException {

		List<Guild> emoteGuilds = getEmoteGuilds();

		List<CustomEmote> uploadedEmote = uploadEmoteInGuildAlreadyExist(customEmotes, emoteGuilds);
		if(!uploadedEmote.isEmpty()) {
			Resources.getCustomEmotes().addAll(uploadedEmote);
		}

		createNewGuildsWithAssignedEmotes(customEmotes, emoteGuilds);

		return customEmotes;
	}

	private static void createNewGuildsWithAssignedEmotes(List<CustomEmote> customEmotes, List<Guild> emoteGuilds) {
		int j = 0;

		while(!customEmotes.isEmpty()) {
			Yuumi.getJda().createGuild("Yuumi Emotes Guild " + (emoteGuilds.size() + j)).complete();
			j++;

			List<CustomEmote> listEmoteForCreatedGuild = new ArrayList<>();

			int numberOfEmoteForNewGuild = 0;

			while(numberOfEmoteForNewGuild < MAX_EMOTE_BY_GUILD && !customEmotes.isEmpty()) {
				listEmoteForCreatedGuild.add(customEmotes.get(0));
				customEmotes.remove(0);
				numberOfEmoteForNewGuild++;
			}

			Yuumi.getEmotesNeedToBeUploaded().add(listEmoteForCreatedGuild);
		}
	}

	private static List<CustomEmote> uploadEmoteInGuildAlreadyExist(List<CustomEmote> customEmotes, List<Guild> emoteGuilds)
			throws IOException {
		List<CustomEmote> emotesUploaded = new ArrayList<>();

		for(Guild guild : emoteGuilds) {
			List<Emote> emotes = getNonAnimatedEmoteOfTheGuild(guild);

			int emotesSize = emotes.size();

			while(emotesSize < MAX_EMOTE_BY_GUILD && !customEmotes.isEmpty()) {
				CustomEmote customEmote = customEmotes.get(0);
				Icon icon = Icon.from(customEmote.getFile());
				Emote emote = guild.createEmote(customEmote.getName(), icon, guild.getPublicRole()).complete();

				emotesSize++;

				customEmote.setEmote(emote);
				emotesUploaded.add(customEmote);
				customEmotes.remove(0);
			}
		}
		return emotesUploaded;
	}

	private static List<Emote> getNonAnimatedEmoteOfTheGuild(Guild guild) {
		List<Emote> emotes = guild.getEmotes();

		List<Emote> emotesNonAnimated = new ArrayList<>();
		for(Emote emote : emotes) {
			if(!emote.isAnimated()) {
				emotesNonAnimated.add(emote);
			}
		}
		return emotes;
	}

	private static List<Guild> getEmoteGuilds() throws IOException {
		List<Guild> emoteGuild = new ArrayList<>();

		for(Guild guild : Yuumi.getJda().getGuilds()) {
			if(guild.getOwnerId().equals(Yuumi.getJda().getSelfUser().getId())) {
				emoteGuild.add(guild);
			}
		}
		return emoteGuild;
	}

	public static void addToMasteryIfIsSame(CustomEmote emote) {
		for(Mastery mastery : Mastery.values()) {
			if(mastery.getName().equalsIgnoreCase(emote.getName())) {
				Resources.getMasteryEmote().put(mastery, emote);
			}
		}
	}

	public static void addToTierIfisSame(CustomEmote emote) {
		for(Tier tier : Tier.values()) {
			if(tier.getTranslationTag().equalsIgnoreCase(emote.getName())) {
				Resources.getTierEmote().put(tier, emote);
			}
		}
	}

	public static void addToChampionIfIsSame(CustomEmote emote) {
		for(Champion champion : Resources.getChampions()) {
			if(champion.getId().equals(emote.getName())) {
				champion.setEmote(emote.getEmote());
			}
		}
	}

	public static void addInfoIconIfSame(CustomEmote emote) {
		if(emote.getEmote() == null) {
			return;
		}

		if(emote.getEmote().getName().equalsIgnoreCase("smallGreenTriangle")) {
			Resources.setGreenTriangleEmote(emote);
			return;
		}

		if(emote.getEmote().getName().equalsIgnoreCase("YuumiMatchInWait")) {
			Resources.setGameToDo(emote);
			return;
		}

		if(emote.getEmote().getName().equalsIgnoreCase("YuumiMatchLose")) {
			Resources.setGameLost(emote);
		}
	}
}