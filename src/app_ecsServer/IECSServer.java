package app_ecsServer;

public interface IECSServer {
	
	public void initService (int numberOfNodes, int	cacheSize, String displacementStrategy);
	
	public void start();
	
	public void stop();
	
	public void shutDown();
	
	public void addNode(int cacheSize, String displacementStrategy);
	
	public void removeNode();
	
}
