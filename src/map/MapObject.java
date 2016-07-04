package map;


public class MapObject 
{
	public static enum MapEntity {
		UNKNOWN, COW, TREE,GRASS, ALLY, OPPONENT, ALLY_CORRAL, OPPONENT_CORRAL, ALLY_CORRAL_SWITCH, OPPONENT_CORRAL_SWITCH, 
	}
	private static final int LAST_SEEN_LIMIT = 40;
	private int lastSeen;
	private String id;
	private MapEntity entity;
	
	public MapObject(String oid, MapEntity entity) 
	{
		this.lastSeen = 0;
		this.id = oid;
		this.entity = entity;
	}

	public int getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(int lastSeen) {
		this.lastSeen = lastSeen;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public MapEntity getEntity() {
		return entity;
	}

	public void setEntity(MapEntity entity) {
		this.entity = entity;
	}


	public void updateLastSeen() 
	{
		this.lastSeen++;
		if(this.lastSeen > LAST_SEEN_LIMIT)
		{
			if(this.entity == MapEntity.COW || this.entity == MapEntity.GRASS || this.entity == MapEntity.OPPONENT)
			{
				this.entity = MapEntity.UNKNOWN;
			}
		}
	}	
}
