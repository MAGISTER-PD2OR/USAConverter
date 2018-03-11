package j1gs4w.ddns.net;

public class User implements Comparable<User> {
	private String UID;
	private String lastPerk;
	private Perk[] perks;
	private long totalExp;
	private long totalKills;
	private long totalPlaytime;	
	private String name;
	private String avatar;
	
	public User(String UID, String lastPerk ,Perk[] perks, long totalExp, long totalKills, long totalPlaytime) {
		this.UID = UID;
		this.lastPerk = lastPerk;
		this.perks = perks;
		this.totalExp = totalExp;
		this.totalKills = totalKills;
		this.totalPlaytime = totalPlaytime;
		
		setOwned();
	}
	
	private final void setOwned() {
		for(Perk perk : perks) {
			perk.setOwner(this);
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("UID: " + UID);
		sb.append("\nLast Perk: " + lastPerk);
		sb.append("\nExp: " + totalExp + "\tKills: " + totalKills + "\tPlaytime: " + totalPlaytime);
		for(Perk p : perks) {
			sb.append("\n" + p.toString());
		}
		
		return sb.toString();
	}
	
	public String getName() {
		return name;
	}
	
	public String getAvatar() {
		return avatar;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	
	public Perk[] getPerks() {
		return perks;
	}
	
	public String getLastPerk() {
		return lastPerk;
	}
	
	public String getUID() {
		return UID;
	}
	
	public long getExp() {
		return totalExp;
	}
	
	public long getKills() {
		return totalKills;
	}
	
	public long getPlaytime() {
		return totalPlaytime;
	}

	@Override
	public int compareTo(User other) {
		return UID.compareTo(other.getUID());
	}
}
