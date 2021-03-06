package ch.kalunight.zoe.model.player_data;

public enum Rank {
  UNKNOWN("", -1), UNRANKED("", -1), IV("4", 0), III("3", 100), II("2", 200), I("1", 300);

  private String name;
  private int value;

  Rank(String name, int value) {
    this.name = name;
    this.value = value;
  }
  
  public static Rank getRankWithValue(int value) {
    for(Rank rank : Rank.values()) {
      if(rank.getValue() == value) {
        return rank;
      }
    }
    return null;
  }
  
  public static Rank getRankWithValueApproximate(int value) {
    for(Rank rank : Rank.values()) {
      
      if((value - rank.getValue()) <= 100) {
        return rank;
      }
    }
    return null;
  }

  public int getValue() {
    return value;
  }

  @Override
  public String toString() {
    return name;
  }
}
