package online.pixelbuilt.pbquests.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.github.eufranio.storage.Persistable;
import online.pixelbuilt.pbquests.PixelBuiltQuests;
import online.pixelbuilt.pbquests.config.ConfigManager;
import online.pixelbuilt.pbquests.quest.Quest;
import online.pixelbuilt.pbquests.quest.QuestLine;
import online.pixelbuilt.pbquests.storage.sql.PlayerData;
import online.pixelbuilt.pbquests.storage.sql.QuestStatus;
import online.pixelbuilt.pbquests.storage.sql.Trigger;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Frani on 10/11/2018.
 */
public class StorageManager {

	private PixelBuiltQuests plugin;

	private Persistable<PlayerData, UUID> playerData;
	private Persistable<QuestStatus, Integer> status;
	private Persistable<Trigger, UUID> triggerData;

	private Map<UUID, List<Trigger>> triggers = Maps.newHashMap();

	public StorageManager(PixelBuiltQuests plugin) {
		this.plugin = plugin;
	}

	public void init() {
		try {
			this.status = Persistable.create(QuestStatus.class, ConfigManager.getConfig().database.url);
			this.playerData = Persistable.create(PlayerData.class, ConfigManager.getConfig().database.url);

			try {
				playerData.objDao.queryRaw("SELECT quests FROM playerData;").close();
			} catch (SQLException | IOException e) {
				try {
					playerData.objDao.executeRaw("ALTER TABLE playerData ADD COLUMN quests VARBINARY;");
				} catch (SQLException ex) {
					e.printStackTrace();
				}
			}

			this.triggerData = Persistable.create(Trigger.class, ConfigManager.getConfig().database.url);
			this.triggerData.objDao.queryForAll().forEach(t -> {
				if (this.triggers.containsKey(t.npc)) {
					this.triggers.get(t.npc).add(t);
				} else {
					List<Trigger> aux = new ArrayList<Trigger>();
					aux.add(t);
					this.triggers.put(t.npc, aux);

				}
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public PlayerData getData(UUID uuid) {
		PlayerData data = this.playerData.getOrCreate(uuid);
		try {
			data.refresh();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return data;
	}

	public void save(PlayerData data) {
		this.playerData.save(data);
	}

	public Map<UUID, List<Trigger>> getTriggers() {
		return ImmutableMap.copyOf(this.triggers);
	}

	public List<Trigger> getTrigger(Entity npc) {
		return this.triggers.get(npc.getUniqueId());
	}

	public void addNPC(Entity npc, QuestLine line, Quest quest, boolean cancelOriginalAction) {
		addTrigger(
				new Trigger(npc.getLocation(), quest, line, Trigger.Type.NPC, npc.getUniqueId(), cancelOriginalAction));
	}

	public void removeNPC(Entity npc) {
		for (Trigger t : this.triggers.remove(npc.getUniqueId())) {
			this.triggerData.delete(t);
		}
	}

	public List<Trigger> getTriggerAt(Location<World> location) {

		return this.triggers.values().stream().flatMap(t -> t.stream())
				.filter(t2 -> t2.location != null
						&& t2.location.getExtent().getUniqueId().equals(location.getExtent().getUniqueId())
						&& t2.location.getBlockPosition().equals(location.getBlockPosition()))
				.collect(Collectors.toList());

	}

	public void addTrigger(Trigger trigger) {
		this.triggerData.save(trigger);
		if (this.triggers.containsKey(trigger.npc)) {
			this.triggers.get(trigger.npc).add(trigger);
		} else {
			List<Trigger> aux = new ArrayList<Trigger>();
			aux.add(trigger);
			this.triggers.put(trigger.npc, aux);

		}
	}

	public void removeTrigger(Trigger trigger) {
		for (Trigger t : this.triggers.remove(trigger.npc)) {
			this.triggerData.delete(t);
		}
	}

}
