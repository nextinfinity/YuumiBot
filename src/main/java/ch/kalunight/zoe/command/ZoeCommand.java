package ch.kalunight.zoe.command;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import ch.kalunight.zoe.ServerData;
import ch.kalunight.zoe.model.dto.DTO;
import ch.kalunight.zoe.repositories.ServerRepository;
import ch.kalunight.zoe.repositories.ServerStatusRepository;
import ch.kalunight.zoe.translation.LanguageManager;
import ch.kalunight.zoe.util.CommandUtil;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public abstract class ZoeCommand extends Command {

  private static final AtomicInteger commandExecuted = new AtomicInteger(0);
  private static final AtomicInteger commandFinishedCorrectly = new AtomicInteger(0);
  private static final AtomicInteger commandFinishedWithError = new AtomicInteger(0);

  private static final Logger logger = LoggerFactory.getLogger(ZoeCommand.class);

  private static final ConcurrentHashMap<Long, DTO.Server> servers = new ConcurrentHashMap<>();

  @Override
  protected void execute(CommandEvent event) {
      Runnable commandRunnable = getCommandRunnable(event);
      ServerData.getCommandsExecutor().execute(commandRunnable);
  }

  private Runnable getCommandRunnable(CommandEvent event) {
    return new Runnable() {

      @Override
      public void run() {
        CommandUtil.sendTypingInFonctionOfChannelType(event);
        logger.info("Command \"{}\" executed", this.getClass().getName());
        commandExecuted.incrementAndGet();

        if(event.getChannelType().equals(ChannelType.TEXT)) {
          try {
            DTO.Server server = ServerRepository.getServerWithGuildId(event.getGuild().getIdLong());
            servers.put(event.getGuild().getIdLong(), server);
            if(server == null) {
              ServerRepository.createNewServer(event.getGuild().getIdLong(), LanguageManager.DEFAULT_LANGUAGE);
              server = ServerRepository.getServerWithGuildId(event.getGuild().getIdLong());
              servers.put(event.getGuild().getIdLong(), server);
            }
          } catch(SQLException e) {
            event.reply("Issue with the db ! Please retry later. Sorry about that :/");
            logger.error("Issue with the db when check if server missing !", e);
            commandFinishedWithError.incrementAndGet();
            return;
          }

          try {
            DTO.ServerStatus status = ServerStatusRepository.getServerStatus(event.getGuild().getIdLong());

            int nbrOfTry = 0;

            while(status.servstatus_inTreatment) {
              nbrOfTry++;
              if(nbrOfTry > 10) {
                logger.warn("The server is in treatment for too long ! Execute the command even though the server is in treatment...");
                break;
              }
              TimeUnit.SECONDS.sleep(1);
              status = ServerStatusRepository.getServerStatus(event.getGuild().getIdLong());
            }

          } catch (SQLException e) {
            event.reply("Issue with the db ! Please retry later. Sorry about that :/");
            logger.error("Issue with the db when check if the server is in treatment !", e);
          } catch (InterruptedException e) {
            logger.error("Thread got interupted !", e);
            Thread.currentThread().interrupt();
          }
        }

        try {
          executeCommand(event);
        } catch (InsufficientPermissionException e) {
          logger.info("Unexpected exception in {} commands. Error : {}", this.getClass().getName(), e.getMessage(), e);
          event.reply(String.format(LanguageManager.getText(getServer(event.getGuild().getIdLong()).serv_language, "deletePlayerMissingPermission"), 
              e.getPermission().getName()));
          commandFinishedCorrectly.incrementAndGet();
          return;
        } catch (Exception e) {
          logger.error("Unexpected exception in {} commands. Error : {}", this.getClass().getName(), e.getMessage(), e);
          event.reply("An error as occured ! It has been saved and sended to the dev. Sorry About that :/");
          commandFinishedWithError.incrementAndGet();
          return;
        }
        logger.info("Command \"{}\" finished correctly", this.getClass().getName());
        commandFinishedCorrectly.incrementAndGet();
      }
    };
  }

  protected abstract void executeCommand(CommandEvent event) throws SQLException;

  public abstract BiConsumer<CommandEvent, Command> getHelpBiConsumer(CommandEvent event);

  public static void clearStats() {
    commandExecuted.set(0);
    commandFinishedCorrectly.set(0);
    commandFinishedWithError.set(0);
  }

  protected static DTO.Server getServer(long guildId){
    return servers.get(guildId);
  }

  public static void clearServerCache() {
    servers.clear();
  }

  public static AtomicInteger getCommandExecuted() {
    return commandExecuted;
  }

  public static AtomicInteger getCommandFinishedCorrectly() {
    return commandFinishedCorrectly;
  }

  public static AtomicInteger getCommandFinishedWithError() {
    return commandFinishedWithError;
  }

}
