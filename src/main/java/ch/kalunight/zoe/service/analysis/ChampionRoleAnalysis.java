 package ch.kalunight.zoe.service.analysis;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.kalunight.zoe.ServerData;
import ch.kalunight.zoe.model.GameQueueConfigId;
import ch.kalunight.zoe.model.dto.SavedMatch;
import ch.kalunight.zoe.repositories.SavedMatchCacheRepository;
import net.rithms.riot.constant.Platform;

public class ChampionRoleAnalysis implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(ChampionRoleAnalysis.class);
  
  private static final int NORMAL_DRAFT_QUEUE_ID = 400;
  
  private static final double MINIMUM_POURCENTAGE_ROLE_COMMUN = 5.0;
  
  private int championId;
  
  private AtomicInteger nbrTop;
  private AtomicInteger nbrJng;
  private AtomicInteger nbrMid;
  private AtomicInteger nbrAdc;
  private AtomicInteger nbrSup;
  
  private AtomicInteger nbrMatch;
  
  private AtomicInteger analysisDone;
  
  public ChampionRoleAnalysis(int championId) {
    this.championId = championId;
  }
  
  @Override
  public void run() {
    
    try {
      String version = SavedMatchCacheRepository.getCurrentLoLVersion(Platform.EUW); //Select EUW region for patch version, usually the last region to be patched.
      
      if(version == null) {
        return;
      }
      List<SavedMatch> matchsToAnalyse = SavedMatchCacheRepository.getMatchsByChampion(championId, GameQueueConfigId.SOLOQ.getId(), NORMAL_DRAFT_QUEUE_ID, version);
      
      int nbrMatchsToAnaylse = matchsToAnalyse.size();
      
      for(SavedMatch matchToAnalyse : matchsToAnalyse) {
        RoleMatchAnalysisWorker roleAnalysisWorker = new RoleMatchAnalysisWorker(matchToAnalyse, this);
        
        ServerData.getDataAnalysisThread().execute(roleAnalysisWorker);
      }
      
      do {
        TimeUnit.MILLISECONDS.sleep(500);
      }while(nbrMatchsToAnaylse != analysisDone.get());
      
      double ratioTop = ((double) nbrTop.get() / nbrMatchsToAnaylse) * 100.0;
      double ratioJng = ((double) nbrJng.get() / nbrMatchsToAnaylse) * 100.0;
      double ratioMid = ((double) nbrMid.get() / nbrMatchsToAnaylse) * 100.0;
      double ratioAdc = ((double) nbrAdc.get() / nbrMatchsToAnaylse) * 100.0;
      double ratioSup = ((double) nbrSup.get() / nbrMatchsToAnaylse) * 100.0;
      
      String rolesList = getRolesString(ratioTop, ratioJng, ratioMid, ratioAdc, ratioSup);
      
      
      
      
    } catch(SQLException e) {
      logger.error("SQL Error with a query", e);
    } catch (InterruptedException e) {
      logger.error("interupted exception !", e);
      Thread.currentThread().interrupt();
    }
    
  }

  private String getRolesString(double ratioTop, double ratioJng, double ratioMid, double ratioAdc, double ratioSup) {
    StringBuilder rolesListBuilder = new StringBuilder();
    
    if(ratioTop > MINIMUM_POURCENTAGE_ROLE_COMMUN) {
      rolesListBuilder.append(ChampionRole.TOP.toString() + ";");
    }
    
    if(ratioJng > MINIMUM_POURCENTAGE_ROLE_COMMUN) {
      rolesListBuilder.append(ChampionRole.JUNGLE.toString() + ";");
    }
    
    if(ratioMid > MINIMUM_POURCENTAGE_ROLE_COMMUN) {
      rolesListBuilder.append(ChampionRole.MID.toString() + ";");
    }
    
    if(ratioAdc > MINIMUM_POURCENTAGE_ROLE_COMMUN) {
      rolesListBuilder.append(ChampionRole.ADC.toString() + ";");
    }
    
    if(ratioSup > MINIMUM_POURCENTAGE_ROLE_COMMUN) {
      rolesListBuilder.append(ChampionRole.SUPPORT.toString() + ";");
    }
    
    return rolesListBuilder.toString();
  }

  public int getChampionId() {
    return championId;
  }

  public AtomicInteger getNbrTop() {
    return nbrTop;
  }

  public AtomicInteger getNbrJng() {
    return nbrJng;
  }

  public AtomicInteger getNbrMid() {
    return nbrMid;
  }

  public AtomicInteger getNbrAdc() {
    return nbrAdc;
  }

  public AtomicInteger getNbrSup() {
    return nbrSup;
  }

  public AtomicInteger getNbrMatch() {
    return nbrMatch;
  }

  public AtomicInteger getAnalysisDone() {
    return analysisDone;
  }
  
}