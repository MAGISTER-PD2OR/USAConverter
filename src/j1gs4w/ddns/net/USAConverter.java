package j1gs4w.ddns.net;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ini4j.Ini;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class USAConverter {

	public static final int longVal = 3;
	public static final int medVal = 2;
	public static final int shortVal = 1;
	public static final int singleVal = 0;

	private static int statPoint;
	private static int strPoint;

	private static ArrayList<User> users = new ArrayList<User>();

	private static Ini ini = null;
	private static File dir = null;
	private static int updateTimer = 3600;
	private static Connection con = null;
	private static String hostname = null;
	private static String username = null;
	private static String password = null;
	private static String tables = null;
	private static int port = 3306;
	private static String[] ext = { "usa" };
	
	private static final int OFFSET = 0x2A;
	private static final int OFFSET2 = - 0x0C + 0x5E;
	private static int ARVERSION = 0;

	// private static byte[] data;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long lastUpdate = 0;

		try {
			ini = new Ini(new File("USAConverter.ini"));
			dir = new File(ini.get("Config", "Directory"));
			hostname = ini.get("Database", "Hostname");
			port = Integer.parseInt(ini.get("Database", "Port"));
			username = ini.get("Database", "Username");
			password = ini.get("Database", "Password");
			tables = ini.get("Database", "Database");
			updateTimer = Integer.parseInt(ini.get("Database", "UpdateTimer"));
			lastUpdate = Integer.parseInt(ini.get("DontChange", "LastUpdate"));

		} catch (IOException e) {
			e.printStackTrace();
		}

		while (true) {
			long startTime = System.currentTimeMillis();
			int userCount = 0;
			try {
				startConnection();
				if (con == null) {
					System.out.println("Failed to establish connection...");
					return;
				}
				Iterator<File> it = FileUtils.iterateFiles(dir, ext, false);
				System.out.println("Fetching stats...");
				while (it.hasNext()) {

					// initialize start and get file
					// statPoint is the initial Location of the first ARVERSION where the stats begin 
					// this can defer by the variables OFFSET OR OFFSET2
					statPoint = 0x31;
					File f = (File) it.next();
					
					// check if file has been modified since last update
					if (f.lastModified() < lastUpdate * 1000)
						continue;
					userCount++;
					
					Path path = Paths.get(f.getPath());
					String fileName = path.getFileName().toString();
					fileName = fileName.substring(4, fileName.length() - 4);
					fileName = String.valueOf(Long.parseLong(fileName, 16));
					byte[] data = Files.readAllBytes(path);

					// read strings
					System.out.println(path.getFileName().toString());
					
					// check which ARVERSION has been used for saving
					// and add some offset if necessary
					ARVERSION = 0;
					byte[] str = Arrays.copyOfRange(data, 0x0C, 0x15);
					if (new String(str).equals("ArVersion")) {
						ARVERSION = 1;
					}
					
					str = Arrays.copyOfRange(data, 0x36, 0x3D);
					boolean bAdd = new String(str).equals("SaveNum");
					if(bAdd) {
						statPoint += OFFSET2;
						ARVERSION = 2;
					} else {					
						str = Arrays.copyOfRange(data, 0x1A, 0x25);
						bAdd = new String(str).equals("IntProperty");
						if (bAdd)
							statPoint += OFFSET;
					}					

					// read in ALL available Strings
					ArrayList<String> statNames = readStrings(data);

					// read total values
					long totalExp = readIntStat(data, longVal);
					long totalKills = readIntStat(data, longVal);
					long totalPlaytime = readIntStat(data, longVal);
					
					if(ARVERSION >= 1) {
						// skip 2 bytes even if custom chars are only supported at ARVERSION >= 2
						long customIndicator = readIntStat(data, shortVal);
						
						// skip saved custom char if there is one
						if(customIndicator != 0 && ARVERSION >= 2) {
							for(int skip=0; skip < 4; skip++) {
								readIntStat(data, singleVal);
							}
							long cosmetics = readIntStat(data, singleVal);
							for(int skip=0; skip < cosmetics; skip++) {
								readIntStat(data, singleVal);
								readIntStat(data, singleVal);
								readIntStat(data, singleVal);
							}
						}
					}
										
					// read last perk
					String lastPerk = getString(data, statNames, shortVal);
					
					// read number of perks
					Perk[] perks = new Perk[(int) readIntStat(data, singleVal)];
											
					// read perks
					for (int i = 0; i < perks.length; i++) {
						// read perk name
						String perkName = getString(data, statNames, shortVal);
						// read offset
						readIntStat(data, shortVal);
						long exp = readIntStat(data, longVal);
						
						if(ARVERSION >= 1) {
							// should add that to the database?
							long prestige = readIntStat(data, longVal);
						}

						// read stat count
						int count = (int) readIntStat(data, singleVal);
						PerkStat[] perkStats = new PerkStat[count];
						
						// read stats
						for (int j = 0; j < count; j++) {
							String statName = getString(data, statNames, shortVal);
							int statVal = (int) readIntStat(data, shortVal);
							perkStats[j] = new PerkStat(statName, statVal);
						}

						// read trait count
						count = (int) readIntStat(data, singleVal);
						Trait[] traits = new Trait[count];

						// read traits
						for (int j = 0; j < count; j++) {
							String traitName = getString(data, statNames, shortVal);
							int traitVal = (int) readIntStat(data, singleVal);
							traits[j] = new Trait(traitName, traitVal);
						}
						perks[i] = new Perk(perkName, exp, perkStats, traits);
					}
					users.add(new User(fileName, lastPerk, perks, totalExp, totalKills, totalPlaytime));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			lastUpdate = System.currentTimeMillis() / 1000;
			Collections.sort(users);

			int[] updates = updateUsers();
			if (updates != null) {
				insertUsers(updates);
			}

			updateStatsAndTraits(updates);
			long stopTime = System.currentTimeMillis();
			System.out.println("Updating took: " + (stopTime - startTime) / 1000 + "s for " + userCount + " users");
			try {
				con.close();
				long time = updateTimer + lastUpdate - System.currentTimeMillis() / 1000;
				System.out.println("Next update in: " + (time / 60) + "min " + (time % 60) + "s...");

				ini.put("DontChange", "LastUpdate", lastUpdate);
				ini.store();
				Thread.sleep(time * 1000);
			} catch (InterruptedException | SQLException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static final void startConnection() {
		System.out.println("Open connection...");
		try {
			MysqlDataSource dataSource = new MysqlDataSource();
			// dataSource.setUseUnicode(true);
			dataSource.setUser(username);
			dataSource.setPassword(password);
			dataSource.setServerName(hostname);
			dataSource.setPort(port);
			con = dataSource.getConnection();
			con.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static final int[] updateUsers() {
		System.out.println("Update users...");
		int[] updates = null;
		try {
			String query = "update " + tables + ".kf2stats set exp = ?, kills = ?, playtime = ?, lastperk = ? where uid = ?";
			PreparedStatement state = con.prepareStatement(query);

			for (User user : users) {
				state.setString(5, user.getUID());
				state.setLong(1, user.getExp());
				state.setLong(2, user.getKills());
				state.setLong(3, user.getPlaytime());
				state.setString(4, user.getLastPerk());
				state.addBatch();
			}
			updates = state.executeBatch();
			con.commit();
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return updates;
	}

	public static final void updateStatsAndTraits(int[] updates) {
		System.out.println("Update stats and traits...");
		int[] upStats = null;
		int[] upTraits = null;
		try {
			String statQuery = "update " + tables + ".perkstats set value = ? where uid = ? and perkname = ? and statname = ?";
			String traitQuery = "update " + tables + ".perktraits set value = ? where uid = ? and perkname = ? and traitname = ?";
			PreparedStatement statState = con.prepareStatement(statQuery);
			PreparedStatement traitState = con.prepareStatement(traitQuery);

			ArrayList<PerkStat> stats = new ArrayList<PerkStat>();
			ArrayList<Trait> traits = new ArrayList<Trait>();
			ArrayList<PerkStat> insStats = new ArrayList<PerkStat>();
			ArrayList<Trait> insTraits = new ArrayList<Trait>();

			for (int i = 0; i < users.size(); i++) {
				User user = users.get(i);
				Perk[] perks = user.getPerks();
				for (int j = 0; j < perks.length; j++) {
					Perk perk = perks[j];
					PerkStat[] perkStats = perk.getPerkStats();
					Trait[] perkTraits = perk.getPerkTraits();
					for (int k = 0; k < perkStats.length; k++) {
						// System.out.println("update stat: " + perk.getName() +
						// "::" + perkStats[k].getName());
						if (updates[i] > 0) {
							stats.add(perkStats[k]);
							statState.setInt(1, perkStats[k].getValue());
							statState.setString(2, user.getUID());
							statState.setString(3, perk.getName());
							statState.setString(4, perkStats[k].getName());
							statState.addBatch();
						} else {
							insStats.add(perkStats[k]);
						}
					}

					for (int k = 0; k < perkTraits.length; k++) {
						// System.out.println("update trait: " + perk.getName()
						// + "::" + perkTraits[k].getName());
						if (updates[i] > 0) {
							traits.add(perkTraits[k]);
							traitState.setInt(1, perkTraits[k].getValue());
							traitState.setString(2, user.getUID());
							traitState.setString(3, perk.getName());
							traitState.setString(4, perkTraits[k].getName());
							traitState.addBatch();
						} else {
							insTraits.add(perkTraits[k]);
						}
					}
				}
			}
			
			upStats = statState.executeBatch();
			con.commit();
			statState.close();
			upTraits = traitState.executeBatch();
			con.commit();
			traitState.close();

			insertStats(stats, upStats, insStats);
			insertTraits(traits, upTraits, insTraits);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static final void insertStats(ArrayList<PerkStat> stats, int[] updates, ArrayList<PerkStat> insStats) {
		System.out.println("Insert stats...");
		try {
			ArrayList<PerkStat> insertStats = new ArrayList<PerkStat>();
			for (int i = 0; i < updates.length; i++) {
				if (updates[i] == 0) {
					insertStats.add(stats.get(i));
				}
			}

			String query = "insert into " + tables + ".perkstats (uid, perkname, statname, value)" + " values(?, ?, ?, ?)";
			PreparedStatement state = con.prepareStatement(query);

			for (PerkStat stat : insertStats) {
				// System.out.println("insert stat: " +
				// stat.getOwner().getName() + "::" + stat.getName());
				state.setString(1, stat.getOwner().getOwner().getUID());
				state.setString(2, stat.getOwner().getName());
				state.setString(3, stat.getName());
				state.setLong(4, stat.getValue());
				state.addBatch();
			}

			for (PerkStat stat : insStats) {
				// System.out.println("insert stat: " +
				// stat.getOwner().getName() + "::" + stat.getName());
				state.setString(1, stat.getOwner().getOwner().getUID());
				state.setString(2, stat.getOwner().getName());
				state.setString(3, stat.getName());
				state.setLong(4, stat.getValue());
				state.addBatch();
			}
			state.executeBatch();
			con.commit();
			state.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static final void insertTraits(ArrayList<Trait> traits, int[] updates, ArrayList<Trait> insTraits) {
		System.out.println("Insert traits...");
		try {
			ArrayList<Trait> insertTraits = new ArrayList<Trait>();
			for (int i = 0; i < updates.length; i++) {
				if (updates[i] == 0) {
					insertTraits.add(traits.get(i));
				}
			}

			String query = "insert into " + tables + ".perktraits (uid, perkname, traitname, value)" + " values(?, ?, ?, ?)";
			PreparedStatement state = con.prepareStatement(query);

			for (Trait trait : insertTraits) {
				// System.out.println("insert trait: " +
				// trait.getOwner().getName() + "::" + trait.getName());
				state.setString(1, trait.getOwner().getOwner().getUID());
				state.setString(2, trait.getOwner().getName());
				state.setString(3, trait.getName());
				state.setLong(4, trait.getValue());
				state.addBatch();
			}

			for (Trait trait : insTraits) {
				// System.out.println("insert trait: " +
				// trait.getOwner().getName() + "::" + trait.getName());
				state.setString(1, trait.getOwner().getOwner().getUID());
				state.setString(2, trait.getOwner().getName());
				state.setString(3, trait.getName());
				state.setLong(4, trait.getValue());
				state.addBatch();
			}
			state.executeBatch();
			con.commit();
			state.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static final void insertUsers(int[] updates) {
		System.out.println("Insert users...");
		String startQuery = "http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=9D2748958D912CA58DC66BB5D204275B&format=json&steamids=";
		StringBuffer nameQuery = new StringBuffer(startQuery);
		try {
			Map<String, JSONObject> map = new HashMap<String, JSONObject>();
			ArrayList<User> insertUsers = new ArrayList<User>();

			for (int i = 0; i < updates.length; i++) {
				if (updates[i] == 0) {
					nameQuery.append(users.get(i).getUID() + ",");
					insertUsers.add(users.get(i));
					if (insertUsers.size() % 100 == 0) {
						System.out.println("Get next 100 playernames...");
						JSONObject json = new JSONObject(IOUtils.toString(new URL(nameQuery.toString()), Charset.forName("UTF-8")));
						JSONArray players = json.getJSONObject("response").getJSONArray("players");

						for (int j = 0; j < players.length(); j++) {
							map.put(players.getJSONObject(j).getString("steamid"), players.getJSONObject(j));
						}
						nameQuery = new StringBuffer(startQuery);
					}
				}
			}

			JSONObject json = new JSONObject(IOUtils.toString(new URL(nameQuery.toString()), Charset.forName("UTF-8")));
			JSONArray players = json.getJSONObject("response").getJSONArray("players");
			for (int j = 0; j < players.length(); j++) {
				map.put(players.getJSONObject(j).getString("steamid"), players.getJSONObject(j));
			}

			String query = "SET NAMES utf8mb4";
			Statement stmt = con.createStatement();
			stmt.execute(query);
			
			query = "insert into " + tables + ".kf2stats (uid, exp, kills, playtime, lastperk, playername, avatar)" + " values(?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement state = con.prepareStatement(query);

			int i = 0;
			for (User user : insertUsers) {
				state.setString(1, user.getUID());
				state.setLong(2, user.getExp());
				state.setLong(3, user.getKills());
				state.setLong(4, user.getPlaytime());
				state.setString(5, user.getLastPerk());
				if (map.get(user.getUID()) != null) {
					state.setString(6, map.get(user.getUID()).getString("personaname"));
					state.setString(7, map.get(user.getUID()).getString("avatar"));
				} else {
					state.setString(6, "");
					state.setString(7, "");
				}
				i++;
				state.addBatch();
			}
			state.executeBatch();
			con.commit();
			state.close();
		} catch (SQLException | JSONException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static final String getString(byte[] data, ArrayList<String> statNames, int length) {
		int idx = (int) readIntStat(data, length);
		return statNames.get(idx - 1);
	}

	public static final ArrayList<String> readStrings(byte[] data) {
		int readAt = 0x2D;
		if (statPoint != 0x31) {
			readAt += statPoint - 0x31;
		}
		strPoint = (int) (statPoint + readInt(data, readAt, longVal));

		int length = readIntString(data, strPoint, longVal);
		readString(data, strPoint, length);
		length = readIntString(data, strPoint, longVal);
		readString(data, strPoint, length);

		final int strLength = readIntString(data, strPoint, longVal);
		// skip unknown bytes
		strPoint += 8;

		final int startRead = strPoint;

		ArrayList<String> statNames = new ArrayList<String>();
		while (startRead + strLength > strPoint) {
			length = readIntString(data, strPoint, longVal);
			statNames.add(readString(data, strPoint, length));
		}
		return statNames;
	}

	public static final String readString(byte[] data, int start, int length) {
		strPoint += length;
		byte[] str = Arrays.copyOfRange(data, start, start + length);
		return new String(str);
	}

	public static final long readIntStat(byte[] data, int length) {
		long result = readInt(data, statPoint, length);
		statPoint += length + 1;
		return result;
	}

	public static final int readIntString(byte[] data, int start, int length) {
		int result = (int) readInt(data, strPoint, length);
		strPoint += length + 1;
		return result;
	}

	public static final long readInt(byte[] data, int start, int length) {
		long result = 0;
		for (int i = start + length; i >= start; i--) {
			result = result << 8;
			result += Byte.toUnsignedInt((data[i]));
		}
		return result;
	}

}
