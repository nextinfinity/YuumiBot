package ch.kalunight.zoe;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import ch.kalunight.zoe.model.dto.DTO;
import ch.kalunight.zoe.repositories.ServerStatusRepository;
import net.dv8tion.jda.api.entities.TextChannel;

public class ServerData {

  private static final Logger logger = LoggerFactory.getLogger(ServerData.class);

  private static final List<DTO.Server> serversAskedTreatment = Collections.synchronizedList(new ArrayList<DTO.Server>()); 

  private static final ConcurrentHashMap<String, Boolean> serversIsInTreatment = new ConcurrentHashMap<>();

  private static final Timer serverCheckerThreadTimer = new Timer("ServerChecker-Timer-Executor");

  public static final int NBR_PROC = Runtime.getRuntime().availableProcessors();

  private static final ThreadPoolExecutor SERVER_EXECUTOR =
      new ThreadPoolExecutor(NBR_PROC, NBR_PROC, 3, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

  private static final ThreadPoolExecutor INFOCARDS_GENERATOR =
      new ThreadPoolExecutor(NBR_PROC, NBR_PROC, 3, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
  
  private static final ThreadPoolExecutor PLAYERS_DATA_WORKER =
      new ThreadPoolExecutor(NBR_PROC, NBR_PROC, 3, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
  
  private static final ThreadPoolExecutor MATCHS_WORKER = 
      new ThreadPoolExecutor(NBR_PROC, NBR_PROC, 3, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

  /**
   * Used by event waiter, define in {@link Zoe#main(String[])}
   */
  private static final ScheduledThreadPoolExecutor RESPONSE_WAITER = new ScheduledThreadPoolExecutor(NBR_PROC);

  private ServerData() {
    // Hide public default constructor
  }

  static {
    logger.info("ThreadPools has been lauched with {} threads", NBR_PROC);
    SERVER_EXECUTOR.setThreadFactory(new ThreadFactoryBuilder().setNameFormat("Zoe Server-Executor-Thread %d").build());
    INFOCARDS_GENERATOR.setThreadFactory(new ThreadFactoryBuilder().setNameFormat("Zoe InfoCards-Generator-Thread %d").build());
    PLAYERS_DATA_WORKER.setThreadFactory(new ThreadFactoryBuilder().setNameFormat("Zoe Players-Data-Worker %d").build());
    MATCHS_WORKER.setThreadFactory(new ThreadFactoryBuilder().setNameFormat("Zoe Match-Worker-Thread %d").build());
    RESPONSE_WAITER.setThreadFactory(new ThreadFactoryBuilder().setNameFormat("Zoe Response-Waiter-Thread %d").build());
  }

  /**
   * Check if the server will be refreshed or if is actually in treatment.
   * @param server to check
   * @return true if the server is in treatment or if he asked to be treated
   * @throws SQLException 
   */
  public static boolean isServerWillBeTreated(DTO.Server server) throws SQLException {

    boolean serverAskedTreatment = false;
    for(DTO.Server serverWhoAsk : serversAskedTreatment) {
      if(serverWhoAsk.serv_guildId == server.serv_guildId) {
        serverAskedTreatment = true;
      }
    }
    
    DTO.ServerStatus serverStatus = ServerStatusRepository.getServerStatus(server.serv_guildId);
    
    return serverAskedTreatment || serverStatus.servstatus_inTreatment;
  }

  public static void shutDownTaskExecutor(TextChannel channel) throws InterruptedException {

    logger.info("Start to shutdown Response Waiter, this can take 5 minutes max...");
    channel.sendMessage("Start to shutdown Response Waiter, this can take 5 minutes max...").complete();
    RESPONSE_WAITER.shutdown();

    RESPONSE_WAITER.awaitTermination(5, TimeUnit.MINUTES);
    if(!RESPONSE_WAITER.isShutdown()) {
      RESPONSE_WAITER.shutdownNow();
    }
    logger.info("Shutdown of Response Waiter has been completed !");
    channel.sendMessage("Shutdown of Response Waiter has been completed !").complete();

    logger.info("Start to shutdown Servers Executor, this can take 5 minutes max...");
    channel.sendMessage("Start to shutdown Servers Executor, this can take 5 minutes max...").complete();
    SERVER_EXECUTOR.shutdown();

    SERVER_EXECUTOR.awaitTermination(5, TimeUnit.MINUTES);
    if(!SERVER_EXECUTOR.isShutdown()) {
      SERVER_EXECUTOR.shutdownNow();
    }
    logger.info("Shutdown of Servers Executor has been completed !");
    channel.sendMessage("Shutdown of Servers Executor has been completed !").complete();

    logger.info("Start to shutdown InfoCards Generator, this can take 5 minutes max...");
    channel.sendMessage("Start to shutdown InfoCards Generator, this can take 5 minutes max...").complete();
    INFOCARDS_GENERATOR.shutdown();

    INFOCARDS_GENERATOR.awaitTermination(5, TimeUnit.MINUTES);
    if(!INFOCARDS_GENERATOR.isShutdown()) {
      INFOCARDS_GENERATOR.shutdownNow();
    }
    logger.info("Shutdown of InfoCards Generator has been completed !");
    channel.sendMessage("Shutdown of InfoCards Generator has been completed !").complete();
    
    logger.info("Start to shutdown Players Data Worker, this can take 5 minutes max...");
    channel.sendMessage("Start to shutdown Players Data Worker, this can take 5 minutes max...").complete();
    PLAYERS_DATA_WORKER.shutdown();

    PLAYERS_DATA_WORKER.awaitTermination(5, TimeUnit.MINUTES);
    if(!PLAYERS_DATA_WORKER.isShutdown()) {
      PLAYERS_DATA_WORKER.shutdownNow();
    }
    logger.info("Shutdown of Players Data Worker has been completed !");
    channel.sendMessage("Shutdown of Players Data Worker has been completed !").complete();
    
    channel.sendMessage("Start to shutdown Match Worker, this can take 5 minutes max...").complete();
    MATCHS_WORKER.shutdown();

    MATCHS_WORKER.awaitTermination(5, TimeUnit.MINUTES);
    if(!MATCHS_WORKER.isShutdown()) {
      MATCHS_WORKER.shutdownNow();
    }
    logger.info("Shutdown of Match Worker has been completed !");
    
    channel.sendMessage("Shutdown of Match Worker has been completed !").complete();
  }
  
  public static ConcurrentMap<String, Boolean> getServersIsInTreatment() {
    return serversIsInTreatment;
  }

  public static List<DTO.Server> getServersAskedTreatment() {
    return serversAskedTreatment;
  }

  public static ThreadPoolExecutor getServerExecutor() {
    return SERVER_EXECUTOR;
  }

  public static ThreadPoolExecutor getInfocardsGenerator() {
    return INFOCARDS_GENERATOR;
  }

  public static ThreadPoolExecutor getPlayersDataWorker() {
    return PLAYERS_DATA_WORKER;
  }
  
  public static ScheduledThreadPoolExecutor getResponseWaiter() {
    return RESPONSE_WAITER;
  }
  
  public static ThreadPoolExecutor getMatchsWorker() {
    return MATCHS_WORKER;
  }

  public static Timer getServerCheckerThreadTimer() {
    return serverCheckerThreadTimer;
  }

}
