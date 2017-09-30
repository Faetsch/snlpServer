import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;


public class GameServer implements Runnable
{
	Socket[] players;
	private double startNanoTime = System.nanoTime();
    private double lastNanoTime = startNanoTime;
    private double elapsedTime;
    private double absoluteTime;
    private GameState currentGameState = new GameState();
    private int soldierSpeed = 150;
    private Shield[] shields = new Shield[2];
    ObjectInputStream[] oisplayer = new ObjectInputStream[2];
    ObjectOutputStream[] oosplayer = new ObjectOutputStream[2];
	ArrayList<String> inputPlayer1 = new ArrayList<String>();
	ArrayList<String> inputPlayer2 = new ArrayList<String>();
	DatagramSocket[] datagramPlayer = new DatagramSocket[2];

	public GameServer(Socket[] players)
	{
		this.players = players;
		System.out.println("New GameServer: " + this.toString());

	}

	@Override
	public void run()
	{
		try
		{
			initGameState();
			initStreams();
			oosplayer[0].writeInt(0);
			oosplayer[0].flush();
			oosplayer[1].writeInt(1);
			oosplayer[1].flush();
//			new Thread()
//			{
//				public void run()
//				{
					try
					{
						while((!players[0].isClosed() && !players[1].isClosed()) || currentGameState.wonBy == 2)
						{
							long currentNanoTime = System.nanoTime();
					        absoluteTime = (currentNanoTime - startNanoTime) / 1000000000.0;
					        elapsedTime = (currentNanoTime - lastNanoTime) / 1000000000.0;
					        lastNanoTime = currentNanoTime;
							try
							{
								Thread.sleep(8);
							}

							catch (InterruptedException e)
							{
								e.printStackTrace();
							}
							sendGameStateToClients();
							readGameStateFromClients();
							calculateGameState();
						}
					}
			    	catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					catch (ClassNotFoundException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

//			    }
//			}.start();


		}

		catch (IOException e)
		{
			e.printStackTrace();
		}

		finally
		{
			try
			{
				System.out.println("closing");
				oisplayer[0].close();
				oosplayer[0].close();
				oisplayer[1].close();
				oosplayer[0].close();
			}

			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void readGameStateFromClients() throws ClassNotFoundException, IOException
	{
		inputPlayer1 = (ArrayList<String>) oisplayer[0].readUnshared();
		inputPlayer2 = (ArrayList<String>) oisplayer[1].readUnshared();
	}

	private void initStreams()
	{
		try
		{
			oosplayer[0] = new ObjectOutputStream(players[0].getOutputStream());
			oosplayer[1] = new ObjectOutputStream(players[1].getOutputStream());
			oisplayer[0] = new ObjectInputStream(players[0].getInputStream());
			oisplayer[1] = new ObjectInputStream(players[1].getInputStream());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void calculateGameStatePlayer1()
	{
        currentGameState.getSoldiers()[0][currentGameState.getSelectedSoldier(0)].setVelocity(0, 0, 0, 0);


        if(inputPlayer1.contains("DIGIT1"))
        {
            if(currentGameState.getSoldiers()[0][0].getHp() > 0)
            {
                currentGameState.setSelectedSoldier(0, 0);
            }
        }

        if(inputPlayer1.contains("DIGIT2"))
        {
            if(currentGameState.getSoldiers()[0][1].getHp() > 0)
            {
                currentGameState.setSelectedSoldier(0, 1);
            }
        }

        if(inputPlayer1.contains("DIGIT3"))
        {
            if(currentGameState.getSoldiers()[0][2].getHp() > 0)
            {
                currentGameState.setSelectedSoldier(0, 2);
            }

        }


        if(inputPlayer1.contains("RIGHT"))
        {
            currentGameState.getSoldiers()[0][currentGameState.getSelectedSoldier(0)].addVelocity(soldierSpeed, 0, 0, 0);
        }

        if(inputPlayer1.contains("DOWN"))
        {
            currentGameState.getSoldiers()[0][currentGameState.getSelectedSoldier(0)].addVelocity(0, 0, 0, soldierSpeed);
        }

        if(inputPlayer1.contains("UP"))
        {
            currentGameState.getSoldiers()[0][currentGameState.getSelectedSoldier(0)].addVelocity(0, soldierSpeed, 0, 0);
        }

        if(inputPlayer1.contains("LEFT"))
        {
            currentGameState.getSoldiers()[0][currentGameState.getSelectedSoldier(0)].addVelocity(0, 0, soldierSpeed, 0);
        }

        if(inputPlayer1.contains("SPACE"))
        {
            if(currentGameState.getSoldiers()[0][currentGameState.getSelectedSoldier(0)].getMissileCooldown() <= 0 && currentGameState.getSoldiers()[0][currentGameState.getSelectedSoldier(0)].getHp() > 0)
            {
            	if(currentGameState.getSoldiers()[0][currentGameState.getSelectedSoldier(0)].isLeft())
            	{
                	currentGameState.getMissiles().add(new Missile(currentGameState.getSoldiers()[0][currentGameState.getSelectedSoldier(0)].getPositionX()+currentGameState.getSoldiers()[0][currentGameState.getSelectedSoldier(0)].getWidth(), currentGameState.getSoldiers()[0][currentGameState.getSelectedSoldier(0)].getPositionY()+(currentGameState.getSoldiers()[0][currentGameState.getSelectedSoldier(0)].getHeight() / 2), true));
            	}

            	else
            	{
                	currentGameState.getMissiles().add(new Missile(currentGameState.getSoldiers()[0][currentGameState.getSelectedSoldier(0)].getPositionX()-25, currentGameState.getSoldiers()[0][currentGameState.getSelectedSoldier(0)].getPositionY()+(currentGameState.getSoldiers()[0][currentGameState.getSelectedSoldier(0)].getHeight() / 2), false));
            	}
            	currentGameState.getSoldiers()[0][currentGameState.getSelectedSoldier(0)].setMissileCooldown(3.00);
            }
        }

        if(inputPlayer1.contains("C"))
        {

        }
	}

	private void calculateGameState()
	{
		currentGameState.elapsedTime = this.elapsedTime;
		currentGameState.absoluteTime = this.absoluteTime;
		currentGameState.currentNanoTime = System.nanoTime();
		calculateGameStatePlayer1();
		calculateGameStatePlayer2();

		for(int i = 0; i < 3; i++)
		{
			for(int k = 0; k < 2; k++)
			{
				currentGameState.getSoldiers()[k][i].update(currentGameState.elapsedTime);
				currentGameState.getSoldiers()[k][i].tickCooldown(currentGameState.elapsedTime);
			}
		}

		Iterator<Missile> iterMissile = currentGameState.getMissiles().iterator();

        while(iterMissile.hasNext())
        {
            Missile m = (Missile) iterMissile.next();
            m.update(currentGameState.elapsedTime);
            for(Soldier s : currentGameState.getSoldiers()[0])
            {
                if(s.intersects(m))
                {
                	if(s.getHp() > 0)
                	{
                		iterMissile.remove();
                		s.setHp(s.getHp()-1);
                		if(s.getHp() == 0)
                		{
                			currentGameState.wonBy = 1;
                			resetGame();
                		}
                	}
                }

            }

            for(Soldier s : currentGameState.getSoldiers()[1])
            {
                if(s.intersects(m))
                {
                	if(s.getHp() > 0)
                	{
                		iterMissile.remove();
                		s.setHp(s.getHp()-1);
                		if(s.getHp() == 0)
                		{
                			currentGameState.wonBy = 0;
                			resetGame();
                		}
                	}
                }
             }
         }
	}

	private void calculateGameStatePlayer2()
	{
        currentGameState.getSoldiers()[1][currentGameState.getSelectedSoldier(1)].setVelocity(0, 0, 0, 0);


        if(inputPlayer2.contains("DIGIT1"))
        {
            if(currentGameState.getSoldiers()[1][0].getHp() > 0)
            {
                currentGameState.setSelectedSoldier(1, 0);
            }
        }

        if(inputPlayer2.contains("DIGIT2"))
        {
            if(currentGameState.getSoldiers()[1][1].getHp() > 0)
            {
                currentGameState.setSelectedSoldier(1, 1);
            }
        }

        if(inputPlayer2.contains("DIGIT3"))
        {
            if(currentGameState.getSoldiers()[1][2].getHp() > 0)
            {
                currentGameState.setSelectedSoldier(1, 2);
            }

        }


        if(inputPlayer2.contains("RIGHT"))
        {
            currentGameState.getSoldiers()[1][currentGameState.getSelectedSoldier(1)].addVelocity(soldierSpeed, 0, 0, 0);
        }

        if(inputPlayer2.contains("DOWN"))
        {
            currentGameState.getSoldiers()[1][currentGameState.getSelectedSoldier(1)].addVelocity(0, 0, 0, soldierSpeed);
        }

        if(inputPlayer2.contains("UP"))
        {
            currentGameState.getSoldiers()[1][currentGameState.getSelectedSoldier(1)].addVelocity(0, soldierSpeed, 0, 0);
        }

        if(inputPlayer2.contains("LEFT"))
        {
            currentGameState.getSoldiers()[1][currentGameState.getSelectedSoldier(1)].addVelocity(0, 0, soldierSpeed, 0);
        }

        if(inputPlayer2.contains("SPACE"))
        {
            if(currentGameState.getSoldiers()[1][currentGameState.getSelectedSoldier(1)].getMissileCooldown() <= 0  && currentGameState.getSoldiers()[1][currentGameState.getSelectedSoldier(1)].getHp() > 0)
            {
            	if(currentGameState.getSoldiers()[1][currentGameState.getSelectedSoldier(1)].isLeft())
            	{
                	currentGameState.getMissiles().add(new Missile(currentGameState.getSoldiers()[1][currentGameState.getSelectedSoldier(1)].getPositionX()+currentGameState.getSoldiers()[1][currentGameState.getSelectedSoldier(1)].getWidth(), currentGameState.getSoldiers()[1][currentGameState.getSelectedSoldier(1)].getPositionY()+(currentGameState.getSoldiers()[1][currentGameState.getSelectedSoldier(1)].getHeight() / 2), true));
            	}

            	else
            	{
                	currentGameState.getMissiles().add(new Missile(currentGameState.getSoldiers()[1][currentGameState.getSelectedSoldier(1)].getPositionX()-31, currentGameState.getSoldiers()[1][currentGameState.getSelectedSoldier(1)].getPositionY()+(currentGameState.getSoldiers()[1][currentGameState.getSelectedSoldier(1)].getHeight() / 2), false));
            	}
            	currentGameState.getSoldiers()[1][currentGameState.getSelectedSoldier(1)].setMissileCooldown(3.00);
            }
        }
	}

	private void sendGameStateToClients() throws IOException
	{

		//player1
		for(int c = 0; c < 2; c++)
		{
			oosplayer[c].writeUnshared(currentGameState);
			oosplayer[c].writeInt(currentGameState.getSelectedSoldier(c));
			oosplayer[c].writeInt(currentGameState.wonBy);
			for(int i = 0; i < 3; i++)
			{
				for(int k = 0; k < 2; k++)
				{
					oosplayer[c].writeDouble(currentGameState.getSoldiers()[k][i].getPositionX());
					oosplayer[c].writeDouble(currentGameState.getSoldiers()[k][i].getPositionY());
					oosplayer[c].writeInt(currentGameState.getSoldiers()[k][i].getHp());
				}
			}

//			oosplayer[c].writeUnshared(currentGameState.missiles);

			oosplayer[c].writeInt(currentGameState.getMissiles().size());
			for(int i = 0; i < currentGameState.getMissiles().size(); i++)
			{
				oosplayer[c].writeDouble(currentGameState.getMissiles().get(i).getPositionX());
				oosplayer[c].writeDouble(currentGameState.getMissiles().get(i).getPositionY());
				oosplayer[c].writeBoolean(currentGameState.getMissiles().get(i).left);
			}

		}
		oosplayer[0].flush();
		oosplayer[1].flush();

	}

	private void initGameState()
	{
        for(int i = 0; i < 3; i++)
        {
            currentGameState.getSoldiers()[0][i] = new Soldier();
            currentGameState.getSoldiers()[1][i] = new Soldier();
            currentGameState.getSoldiers()[0][i].setLeft(true);
            currentGameState.getSoldiers()[1][i].setLeft(false);
            currentGameState.getSoldiers()[0][i].setPosition(75 + 120* i, 50 + 200 * i);
            currentGameState.getSoldiers()[1][i].setPosition(540 + 120*i, 50 + 200*i);
            currentGameState.getSoldiers()[0][i].setDuration(0.03);
            currentGameState.getSoldiers()[1][i].setDuration(0.03);
            currentGameState.wonBy = 2;
        }

        inputPlayer1.clear();
        inputPlayer2.clear();

        currentGameState.setSelectedSoldier(0, 1);
        currentGameState.setSelectedSoldier(1, 1);
	}

	private void resetGame()
	{
//		currentGameState = new GameState();

		try
		{
			Thread.sleep(3000);
		}

		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		initGameState();
		startNanoTime = System.nanoTime();
		elapsedTime = 0;
		absoluteTime = 0;
		currentGameState.currentNanoTime = System.nanoTime();
		currentGameState.elapsedTime = 0;
		currentGameState.absoluteTime = 0;
	}

}
