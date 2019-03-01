package ch.kalunight.zoe.model;

import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.core.entities.Message;

public class ControlPannel {
  
  private List<Message> infoPanel;
  private List<InfoCard> infoCards;

  public ControlPannel() {
    this.infoPanel = new ArrayList<>();
    this.infoCards = new ArrayList<>();
  }

  public List<Message> getInfoPanel() {
    return infoPanel;
  }

  public void setInfoPanel(List<Message> infoPanel) {
    this.infoPanel = infoPanel;
  }

  public List<InfoCard> getInfoCards() {
    return infoCards;
  }

  public void setInfoCards(List<InfoCard> infoCards) {
    this.infoCards = infoCards;
  }
  
}