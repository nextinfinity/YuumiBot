package ch.kalunight.zoe.command;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import ch.kalunight.zoe.ServerData;
import ch.kalunight.zoe.model.Server;
import ch.kalunight.zoe.model.config.ServerConfiguration;
import ch.kalunight.zoe.model.config.option.ConfigurationOption;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

public class ConfigCommand extends Command{
  
  private EventWaiter waiter;
  
  public ConfigCommand(EventWaiter waiter) {
    this.name = "config";
    this.help = "Open an interactive message to configure the server.";
    this.hidden = false;
    this.ownerCommand = false;
    Permission[] permissionRequired = {Permission.MANAGE_CHANNEL};
    this.userPermissions = permissionRequired;
    this.guildOnly = true;
    this.waiter = waiter;
    this.helpBiConsumer = getHelpMethod();
  }
  
  @Override
  protected void execute(CommandEvent event) {
    CommandUtil.sendTypingInFonctionOfChannelType(event);
    
    Server server = ServerData.getServers().get(event.getGuild().getId());
    
    OrderedMenu.Builder builder = new OrderedMenu.Builder()
        .addUsers(event.getAuthor())
        .allowTextInput(false)
        .setTimeout(2, TimeUnit.MINUTES)
        .useNumbers()
        .setText("Here my options:")
        .setDescription("Configuration Choices:")
        .useCancelButton(true)
        .setEventWaiter(waiter);
    
    ServerConfiguration serverConfiguration = server.getConfig();
    
    List<ConfigurationOption> options = serverConfiguration.getAllConfigurationOption();
    for(ConfigurationOption option : options) {
      builder.addChoice(option.getChoiceText());
    }
    
    builder.setSelection(getSelectionAction(options, event))
    .setCancel(getCancelAction());
    
    builder.build().display(event.getChannel());
  }
  
  private BiConsumer<Message, Integer> getSelectionAction(List<ConfigurationOption> options, CommandEvent event){
    return new BiConsumer<Message, Integer>() {
      
      @Override
      public void accept(Message messageEmbended, Integer selectionNumber) {
        options.get(selectionNumber - 1).getChangeConsumer(waiter).accept(event);
      }};
  }
  
  private Consumer<Message> getCancelAction(){
    return new Consumer<Message>() {

      @Override
      public void accept(Message message) {
        message.clearReactions();
        message.editMessage("Configuration ended").queue();
      }};
  }
  
  private BiConsumer<CommandEvent, Command> getHelpMethod() {
    return new BiConsumer<CommandEvent, Command>() {
      @Override
      public void accept(CommandEvent event, Command command) {
        CommandUtil.sendTypingInFonctionOfChannelType(event);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name + " command :\n");
        stringBuilder.append("--> `>" + name + " " + "` : " + help);

        event.reply(stringBuilder.toString());
      }
    };
  }
  
}
