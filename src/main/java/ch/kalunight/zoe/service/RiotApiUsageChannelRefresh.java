package ch.kalunight.zoe.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.PieStyler.AnnotationType;
import org.knowm.xchart.style.Styler.ChartTheme;
import ch.kalunight.zoe.Zoe;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.rithms.riot.api.request.ratelimit.RateLimitRequestTank;
import net.rithms.riot.constant.Platform;

public class RiotApiUsageChannelRefresh implements Runnable {

  private static TextChannel rapiInfoChannel;

  @Override
  public void run() {
    if(rapiInfoChannel != null && Zoe.getMinuteApiTank() != null) {
      synchronized(rapiInfoChannel) {

        cleanChannel();

        RateLimitRequestTank minutesRAPITank = Zoe.getMinuteApiTank();

        ArrayList<byte[]> graphs = new ArrayList<>();
        List<Platform> platformOrder = new ArrayList<>();
        List<Message> descriptions = new ArrayList<>();
        for(Platform platform : Platform.values()) {
          long numberOfRequestRemaining = minutesRAPITank.getNumberOfRequestRemaining(platform);

          PieChart pieChart = new PieChartBuilder()
              .title("Request data for " + platform.getName())
              .theme(ChartTheme.GGPlot2)
              .build();

          PieStyler styler = pieChart.getStyler();
          styler.setAntiAlias(true);
          styler.setAnnotationType(AnnotationType.LabelAndValue);
          styler.setAnnotationDistance(1.1);
          styler.setHasAnnotations(true);

          pieChart.addSeries("Calls Used", minutesRAPITank.getNumberOfRequestForThisPeriod() - numberOfRequestRemaining);
          pieChart.addSeries("Calls avaible", numberOfRequestRemaining);

          try {
            graphs.add(BitmapEncoder.getBitmapBytes(pieChart, BitmapFormat.PNG));
            platformOrder.add(platform);
            
            MessageBuilder description = new MessageBuilder();
            description.append("Status of Api for " + platform.getName() + ". Max Calls : " 
                + minutesRAPITank.getNumberOfRequestForThisPeriod() + " Calls Used : " 
                + (minutesRAPITank.getNumberOfRequestForThisPeriod() - numberOfRequestRemaining));
            
            descriptions.add(description.build());
          } catch(IOException e) {
            rapiInfoChannel.sendMessage("Got an error when generating graph for " + platform.getName()).queue();
          }
        }

        for(int i = 0; i < graphs.size(); i++) {
          rapiInfoChannel.sendFile(graphs.get(i), "graphFor" + platformOrder.get(i).getName() + ".png", descriptions.get(i)).queue();
        }
      }
    }
  }

  private void cleanChannel() {
    List<Message> messagesToDelete = rapiInfoChannel.getIterableHistory().stream()
        .collect(Collectors.toList());

    try {
      rapiInfoChannel.purgeMessages(messagesToDelete);
    }catch(InsufficientPermissionException e) {
      rapiInfoChannel.sendMessage("I cannot clean all the channel, please give me the right to delete messages of all peoples").queue();
      List<Message> onlyMyMessagesToDelete = rapiInfoChannel.getIterableHistory().stream()
          .filter(m -> m.getAuthor().equals(Zoe.getJda().getSelfUser()))
          .collect(Collectors.toList());
      rapiInfoChannel.purgeMessages(onlyMyMessagesToDelete);
    }
  }

  public static TextChannel getRapiInfoChannel() {
    return rapiInfoChannel;
  }

  public static void setRapiInfoChannel(TextChannel rapiInfoChannel) {
    RiotApiUsageChannelRefresh.rapiInfoChannel = rapiInfoChannel;
  }

}
