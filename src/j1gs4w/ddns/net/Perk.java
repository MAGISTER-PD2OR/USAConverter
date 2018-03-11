package j1gs4w.ddns.net;

public class Perk {
	private String name;
	private long exp;
	private PerkStat[] stats;
	private Trait[] traits;
	private User owner;
	
	public Perk(String name, long exp, PerkStat[] stats, Trait[] traits) {
		this.name = name;
		this.exp = exp;
		this.stats = stats;
		this.traits = traits;
		
		setOwned();
	}
	
	public final User getOwner() {
		return owner;
	}
	
	public final void setOwner(User owner) {
		this.owner = owner;
	}
	
	private final void setOwned() {
		for(PerkStat stat : stats) {
			stat.setOwner(this);
		}
		for(Trait trait : traits) {
			trait.setOwner(this);
		}
	}
	
	public final String getName() {
		return name;
	}
	
	public final PerkStat[] getPerkStats() {
		return stats;
	}
	
	public final Trait[] getPerkTraits() {
		return traits;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Perk: " + name + "\nExp: " + exp + "\nStats:\n");
		for(int i=0; i<stats.length; i++) {
			sb.append("\t"+ stats[i].toString() + "\n");
		}
		
		sb.append("Traits:\n");
		
		for(int i=0; i<traits.length; i++) {
			sb.append("\t"+ traits[i].toString() + "\n");
		}
		
		return sb.toString();
	}
}

class PerkStat {
	private String name;
	private int value;
	private Perk owner;
	
	PerkStat(String name, int value) {
		this.name = name;
		this.value = value;
	}
	
	public final Perk getOwner() {
		return owner;
	}
	
	public final void setOwner(Perk owner) {
		this.owner = owner;
	}
	
	public String getName() {
		return name;
	}
	
	public int getValue() {
		return value;
	}
	
	public String toString() {
		return name + ": " + value;
	}
}

class Trait {
	private String name;
	private int value;
	private Perk owner;
	
	Trait(String name, int value) {
		this.name = name;
		this.value = value;		
	}	
	
	public final Perk getOwner() {
		return owner;
	}
	
	public final void setOwner(Perk owner) {
		this.owner = owner;
	}
	
	public String getName() {
		return name;
	}
	
	public int getValue() {
		return value;
	}
	
	public String toString() {
		return name + ": " + value;
	}
}