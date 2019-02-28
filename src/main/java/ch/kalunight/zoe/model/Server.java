package ch.kalunight.zoe.model;

import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

public class Server {

  private Guild guild;
  private List<Player> players;
  private List<Team> teams;
  private TextChannel infoChannel;
  private SpellingLangage langage;
  
  public Server(Guild guild, SpellingLangage langage) {
    this.guild = guild;
    this.langage = langage;
    players = new ArrayList<>();
    players = new ArrayList<>();
  }

  public List<Player> getPlayers() {
    return players;
  }

  public void setPlayers(List<Player> players) {
    this.players = players;
  }

  public List<Team> getTeams() {
    return teams;
  }

  public void setTeams(List<Team> teams) {
    this.teams = teams;
  }

  public Guild getGuild() {
    return guild;
  }

  public TextChannel getInfoChannel() {
    return infoChannel;
  }

  public void setInfoChannel(TextChannel infoChannel) {
    this.infoChannel = infoChannel;
  }

  public SpellingLangage getLangage() {
    return langage;
  }

  public void setLangage(SpellingLangage langage) {
    this.langage = langage;
  }
}
