package com.ptibiscuit.igates.data;

import java.util.ArrayList;
import org.bukkit.Location;

public class Volume {
	private Location first;
	private Location end;
	
	public Volume(Location f, Location e)
	{
		this.first = f;
		this.end = e;
	}
	
	public ArrayList<Location> getBlocks()
	{
		ArrayList<Location> locs = new ArrayList<Location>();
		int maxx = (first.getBlockX() > end.getBlockX()) ? first.getBlockX() : end.getBlockX();
		int minx = (first.getBlockX() < end.getBlockX()) ? first.getBlockX() : end.getBlockX();
		int maxy = (first.getBlockY() > end.getBlockY()) ? first.getBlockY() : end.getBlockY();
		int miny = (first.getBlockY() < end.getBlockY()) ? first.getBlockY() : end.getBlockY();
		int maxz = (first.getBlockZ() > end.getBlockZ()) ? first.getBlockZ() : end.getBlockZ();
		int minz = (first.getBlockZ() < end.getBlockZ()) ? first.getBlockZ() : end.getBlockZ();
		for (int fy = miny;fy <= maxy;fy++)
		{
			for (int fx = minx;fx <= maxx;fx++)
			{
				for (int fz = minz;fz <= maxz;fz++)
				{
					locs.add(new Location(first.getWorld(), fx, fy, fz));
				}
			}
		}
		return locs;
	}
	
	public boolean isIn(Location l, double offset)
	{
		
		double maxx = ((first.getBlockX() > end.getBlockX()) ? first.getBlockX() : end.getBlockX()) + offset;
		double minx = ((first.getBlockX() < end.getBlockX()) ? first.getBlockX() : end.getBlockX()) - offset;
		double maxy = ((first.getBlockY() > end.getBlockY()) ? first.getBlockY() : end.getBlockY()) + offset;
		double miny = ((first.getBlockY() < end.getBlockY()) ? first.getBlockY() : end.getBlockY()) - offset;
		double maxz = ((first.getBlockZ() > end.getBlockZ()) ? first.getBlockZ() : end.getBlockZ()) + offset;
		double minz = ((first.getBlockZ() < end.getBlockZ()) ? first.getBlockZ() : end.getBlockZ()) - offset;
		if ((l.getX() >= minx && l.getX() < maxx + 1) && (l.getY() >= miny && l.getY() < maxy + 1) && (l.getZ() >= minz && l.getZ() < maxz + 1))
			return true;
		
		/*for (Location lC : getBlocks())
		{
			if (l.getWorld() == lC.getWorld() && l.getBlockX() == lC.getBlockX() && lC.getBlockY() == l.getBlockY() && lC.getBlockZ() == l.getBlockZ())
				return true;
		}*/
		return false;
	}
	
	public boolean isIn(Location l) {
		return this.isIn(l, 0);
	}

	public Location getFirst() {
		return first;
	}

	public void setFirst(Location first) {
		this.first = first;
	}

	public Location getEnd() {
		return end;
	}

	public void setEnd(Location end) {
		this.end = end;
	}
}
