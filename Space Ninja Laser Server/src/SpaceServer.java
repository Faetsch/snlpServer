
public class SpaceServer
{
	public static void main(String[] args)
	{
		Server server = new Server(3000);
		new Thread(server).start();
	}
}
