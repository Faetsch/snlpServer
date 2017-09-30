import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server implements Runnable
{
	private ServerSocket server;
	private ArrayList<GameServer> gameServerList = new ArrayList<GameServer>();

	public Server(int port)
	{
		try
		{
			server = new ServerSocket(port);
		}

		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void run()
	{
		System.out.println("Space Ninja Laser Server running");
		while(true)
		{
			Socket[] clients = new Socket[2];
			int c = 0;
			try
			{
				while(clients[1] == null)
				{
					clients[c] = server.accept();
					System.out.println("Player " + c + " found");
					c++;
				}

				if(clients[1] != null)
				{
					GameServer gs = new GameServer(clients);
					new Thread(gs).start(); //multithreaded
					gameServerList.add(gs);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

		}

	}
}
