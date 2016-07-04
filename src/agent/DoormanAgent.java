package agent;

import jason.asSemantics.Agent;

public class DoormanAgent extends Agent 
{
	private double closestCorralX = Double.MAX_VALUE;
	private double closestCorralY = Double.MAX_VALUE;
	
	private double dist = Double.MAX_VALUE;
	
	public double getClosestCorralX() 
	{
		return closestCorralX;
	}
	public void setClosestCorralX(double closestCorralX) 
	{
		this.closestCorralX = closestCorralX;
	}
	public double getClosestCorralY() 
	{
		return closestCorralY;
	}
	public void setClosestCorralY(double closestCorralY) 
	{
		this.closestCorralY = closestCorralY;
	}
	public double getDist() {
		return dist;
	}
	public void setDist(double dist) {
		this.dist = dist;
	}
	
	
}
