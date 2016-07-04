package map;

import map.MapObject.MapEntity;


public class GlobalMap {

	private static GlobalMap map;
	public static GlobalMap getInstance()
	{
		return map;
	}
	
	public static void create(int w, int h)
	{
		map = new GlobalMap(w, h); 
	}
	
	private int w;
	private int h;
	
	private MapObject[][] mapRepresentation;
	
	private GlobalMap(int w, int h)
	{
		this.w = w;
		this.h = h;
		
		mapRepresentation = new MapObject[h][w];
		for(int i = 0; i < h; i++)
		{
			for(int j = 0; j < w; j++)
			{
				mapRepresentation[i][j] = new MapObject("unknown", MapEntity.UNKNOWN);
			}
		}
					
	}

	public void set(int ox, int oy, String oid, MapEntity kind) {
		System.out.println("Setting map representation: " + ox + " " + oy + " " + oid + " " + kind);
		mapRepresentation[oy][ox] = new MapObject(oid, kind);
	}
	
	public MapObject get(int x, int y)
	{
		return mapRepresentation[y][x];
	}
	
	public void updateMap()
	{
		for(int i = 0; i < h; i++)
		{
			for(int j = 0; j < w; j++)
			{
				mapRepresentation[i][j].updateLastSeen();					
			}
		}
	}
}