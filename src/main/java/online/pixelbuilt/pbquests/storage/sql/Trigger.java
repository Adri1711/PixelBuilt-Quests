package online.pixelbuilt.pbquests.storage.sql;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;
import online.pixelbuilt.pbquests.config.ConfigManager;
import online.pixelbuilt.pbquests.quest.Quest;
import online.pixelbuilt.pbquests.quest.QuestLine;
import online.pixelbuilt.pbquests.storage.sql.persister.LocationPersister;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.UUID;

/**
 * Created by Frani on 17/12/2017.
 */
@DatabaseTable(tableName = "triggers")
public class Trigger extends BaseDaoEnabled<Trigger, UUID> implements Comparable<Trigger> {

	public Trigger() {
	}

	@DatabaseField(id = true)
	public UUID npc;

	@DatabaseField
	private String line;

	@DatabaseField
	private int questId;

	@DatabaseField
	public Type type;

	@DatabaseField(persisterClass = LocationPersister.class)
	public Location<World> location;

	@DatabaseField
	public boolean cancelOriginalAction = true;

	public Trigger(Location<World> loc, Quest quest, QuestLine line, Type type, UUID npc,
			boolean cancelOriginalAction) {
		this.location = loc;
		this.line = line.getName();
		this.questId = quest.getId();
		this.type = type;
		this.npc = npc == null ? UUID.randomUUID() : npc;
		this.cancelOriginalAction = cancelOriginalAction;
	}

	public Quest getQuest() {
		return ConfigManager.getQuest(this.questId);
	}

	public QuestLine getQuestLine() {
		return ConfigManager.getLine(this.line);
	}

	public enum Type {

		WALK, CLICK, RIGHT_CLICK, LEFT_CLICK, NPC

	}

	public UUID getNpc() {
		return npc;
	}

	public String getLine() {
		return line;
	}

	public int getQuestId() {
		return questId;
	}

	public Type getType() {
		return type;
	}

	public Location<World> getLocation() {
		return location;
	}

	public boolean isCancelOriginalAction() {
		return cancelOriginalAction;
	}

	@Override
	public int compareTo(Trigger o) {

		return getLine().equals(o.getLine()) ? (getQuestId() > o.getQuestId() ? 1 : -1)
				: getLine().compareTo(o.getLine());
	}

}
