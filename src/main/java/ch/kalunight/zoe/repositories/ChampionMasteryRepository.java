package ch.kalunight.zoe.repositories;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.kalunight.zoe.model.dto.DTO;
import ch.kalunight.zoe.model.dto.SavedChampionMastery;
import net.rithms.riot.constant.Platform;

public class ChampionMasteryRepository {
  
  private static final Gson gson = new GsonBuilder().create();
  
  private static final String SELECT_MASTERY_CACHE_WITH_SUMMONER_ID_AND_SERVER = "SELECT " +
      "champion_mastery_cache.champmascache_id, " +
      "champion_mastery_cache.champmascache_summonerid, " +
      "champion_mastery_cache.champmascache_server, " +
      "champion_mastery_cache.champmascache_data " +
      "FROM champion_mastery_cache " +
      "WHERE champion_mastery_cache.champmascache_summonerid = '%s' " +
      "AND champion_mastery_cache.champmascache_server = '%s'";
  
  private static final String INSERT_MASTERY_CACHE = "INSERT INTO champion_mastery_cache "
      + "(champMasCache_summonerId, champMasCache_server, champMasCache_data) VALUES ('%s', '%s', '%s')";
  
  private ChampionMasteryRepository() {
    //hide default public constructor
  }
  
  public static DTO.ChampionMasteryCache getChampionMasteryWithSummonerId(String summonerId, Platform platform) throws SQLException {
    ResultSet result = null;
    try (Connection conn = RepoRessources.getConnection();
        Statement query = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {

      String finalQuery = String.format(SELECT_MASTERY_CACHE_WITH_SUMMONER_ID_AND_SERVER, summonerId, platform.getName());
      result = query.executeQuery(finalQuery);
      int rowCount = result.last() ? result.getRow() : 0;
      if(rowCount == 0) {
        return null;
      }
      return new DTO.ChampionMasteryCache(result);
    }finally {
      RepoRessources.closeResultSet(result);
    }
  }
  
  public static void createMasteryCache(String summonerId, Platform server, SavedChampionMastery championMasteryToCache) throws SQLException {
    try (Connection conn = RepoRessources.getConnection();
        Statement query = conn.createStatement();) {
      
      String finalQuery = String.format(INSERT_MASTERY_CACHE, summonerId, server, gson.toJson(championMasteryToCache));
      query.execute(finalQuery);
    }
  }
  
}
