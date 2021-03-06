package ch.kalunight.zoe.service.match;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.kalunight.zoe.Zoe;
import ch.kalunight.zoe.model.dto.DTO;
import ch.kalunight.zoe.model.dto.DTO.MatchCache;
import ch.kalunight.zoe.riotapi.CachedRiotApi;
import net.rithms.riot.api.endpoints.match.dto.MatchReference;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;

public abstract class MatchReceiverWorker implements Runnable {

  protected static final List<MatchReference> matchsInWork = Collections.synchronizedList(new ArrayList<>());
  
  protected static final Logger logger = LoggerFactory.getLogger(MatchReceiverWorker.class);
  
  protected static final CachedRiotApi riotApi = Zoe.getRiotApi();
  
  protected AtomicBoolean gameLoadingConflict;
  
  protected Platform server;
  
  protected MatchReference matchReference;
  
  protected Summoner summoner;
  
  public MatchReceiverWorker(AtomicBoolean gameLoadingConflict,
      MatchReference matchReference, Platform server, Summoner summoner) {
    this.gameLoadingConflict = gameLoadingConflict;
    this.server = server;
    this.matchReference = matchReference;
    this.summoner = summoner;
    matchsInWork.add(matchReference);
  }
  
  @Override
  public void run() {
    logger.debug("Start to load game {} server {}", matchReference.getGameId(), server.getName());
    DTO.MatchCache matchCache = null;
    try {
      matchCache = Zoe.getRiotApi().getCachedMatch(server, matchReference.getGameId());
    } catch (SQLException e) {
      logger.warn("Unexpected Database error while getting match cache !", e);
    }
    
    if(matchCache != null) {
      //Add the call
      Zoe.getRiotApi().getAllMatchCounter().incrementAndGet();
    }
    
    try {
      runMatchReceveirWorker(matchCache);
    }catch(Exception e){
      logger.error("Unexpected error in match receiver worker", e);
    }finally {
      matchsInWork.remove(matchReference);
    }
  }
  
  protected abstract void runMatchReceveirWorker(MatchCache matchCache);
  
  public static void awaitAll(List<MatchReference> matchsToWait) {
    
    boolean needToWait;
    
    do {
      needToWait = false;
      for(MatchReference matchReference : matchsToWait) {
        if(matchsInWork.contains(matchReference)) {
          needToWait = true;
          break;
        }
      }
      
      if(needToWait) {
        try {
          TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
          logger.error("Thread as been interupt when waiting Match Worker !", e);
          Thread.currentThread().interrupt();
        }
      }
    }while(needToWait);
  }
}
