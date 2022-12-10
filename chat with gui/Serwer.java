
package pakiet;

import java.io.*;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.*;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextArea;
import java.awt.EventQueue;
import java.awt.Color;
import java.awt.Dimension;


public class Serwer{

	private ServerSocket Serwer;
	private boolean ready;
	private ExecutorService group;
	private JFrame frame;
	private JTextArea serwerwyswietlacz; 
	private static Set<String> zbioraktywnychklientow = new HashSet<>(); 
	private static Map<String, Socket> mapawszystkichklientow = new ConcurrentHashMap<>(); 
	private DefaultListModel<String> modelaktywnych = new DefaultListModel<String>(); 
	private DefaultListModel<String> modelall = new DefaultListModel<String>();
	private JList listawszystkichklientow;  
    private JList activelistaklientow; 
   private static int port = 9999;  
	
    public static void main(String[] args) {

    		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Serwer window = new Serwer();  
					window.frame.setVisible(true); 
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	

    	
    } //KONIEC MAIN
    
    	    	public Serwer() {
		grafika();  
		try {

				Serwer = new ServerSocket(port);
			serwerwyswietlacz.append("Server started on port: " + port + "\n"); 
			serwerwyswietlacz.append("Waiting for the clients...\n");
			new nowyklient().start(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
class nowyklient extends Thread 
{
	@Override
public void run ()
{
	while(true){
	
	try{
	Socket client = Serwer.accept();
 

	String username = new DataInputStream(client.getInputStream()).readUTF(); // this will receive the username sent from client register view
	DataOutputStream outklient = new DataOutputStream(client.getOutputStream()); // create an output stream for client


if (zbioraktywnychklientow != null && zbioraktywnychklientow.contains(username)) { 
						outklient.writeUTF("Username already taken");;
					} else {
						mapawszystkichklientow.put(username, client); 
						zbioraktywnychklientow.add(username);

						modelaktywnych.addElement(username); 
						if (!modelall.contains(username)) 
							modelall.addElement(username);
						activelistaklientow.setModel(modelaktywnych); 
						listawszystkichklientow.setModel(modelall);

						serwerwyswietlacz.append("Client " + username + " Connected...\n"); 
						new komunikacja(client, username).start(); 

						new Preparelistaklientow().start(); 	
								} 
		
	}
	 catch (IOException ioex) {
	ioex.printStackTrace();	
	}
					 catch (Exception e) {
					e.printStackTrace();
				}
				
}		
}	
}  //KONIEC WATKU nowyklient



   class komunikacja extends Thread 
    {
    	
    Socket s;
String Id; 	
     		
 //metoday conhandl ktora pobiera socket klienta i ic dalej nie robi    		
    private komunikacja (Socket s, String username){
    	this.s = s;
    	this.Id = username;
    	
    }	
    	

    @Override
    	public void run (){

			while (listawszystkichklientow != null && !mapawszystkichklientow.isEmpty()){
try{

String message = new DataInputStream(s.getInputStream()).readUTF(); 
System.out.println("message read ==> " + message); 
System.out.println("message read ==> " + message); 
String[] msgList = message.split(":"); 
//POWYZEJ JEST STWORZONA TABLICA  znak : oznacza zmiane instrukcji..
//U MNIE TO BYLO String[]messageSplit czyli ta sama metoda

if (msgList[0].equalsIgnoreCase("multityp1"))//wiec mamy nasz multityp1
{
String[] sendToList = msgList[1].split(","); 
//POWYZEJ kolejna tablica sendtolist lista userow do ktorych bedzie wysylane...tutan spli 1 z podzlaem znak , oki
for (String usr : sendToList) { 
							try {
								if (zbioraktywnychklientow.contains(usr)) { 


//OKI POWYZSZE DO PRZEROBIENIA
//moze?

new DataOutputStream(((Socket) mapawszystkichklientow.get(usr)).getOutputStream()).writeUTF("< " + Id + " >" + msgList[2]); 

								}//ZAMYKAM IF




							}//ZAMYKAM TRY
 catch (Exception e) { 
								e.printStackTrace();
							}//ZAMYKAM CATCH
						} //ZAMYKAM FOR

}  //if (msgList[0].equalsIgnoreCase("multityp1")) ZAMYKAM IF


else if (msgList[0].equalsIgnoreCase("typ1_broadcast"))
{


try{
for (String ch : zbioraktywnychklientow)
{

	new DataOutputStream(((Socket) mapawszystkichklientow.get(ch)).getOutputStream()).writeUTF("< " + Id + " >" + msgList[1]);

}
}
catch(Exception e){
e.printStackTrace();
}//ZAMYKAM CATCH


}//ZAMYKA ELSE IF
else if (msgList[0].equalsIgnoreCase("exit"))
{
zbioraktywnychklientow.remove(Id); 
serwerwyswietlacz.append(Id + " disconnected....\n");
new Preparelistaklientow().start(); 

for (String rd : zbioraktywnychklientow){

try{


new DataOutputStream(((Socket) mapawszystkichklientow.get(rd)).getOutputStream()).writeUTF( Id + " is disconnected " );

}
catch (Exception e) { 
	e.printStackTrace();
	}							}

new Preparelistaklientow().start(); 

}
modelaktywnych.removeElement(Id); 
activelistaklientow.setModel(modelaktywnych);


}  //ZAMYKAM TRY ALE JESZCZE NIE ZAMKNIETE WHILE

catch(Exception e)
{
e.printStackTrace();
}
}//ZAKNIETE WHILE!!
}  //ZAMKNIETYRUN W  CONNECTION MANAGER 
    }  //ZAMKNIETY CONNECTION MANAGER PONIZEJ STARE KODY DO CONNECTION MANAGER
    
    //w pnizszaym watku ponieramy aktywnych userow i suawiamy ich w odpowiednim formacie tzn z znakiem " , "..
    //leciimy po activuserlist gdzie maja wpisana tylko username

class Preparelistaklientow extends Thread  {
@Override

public void run()
{
try{
	String ids = "";
	for (String xd : zbioraktywnychklientow)
	{
ids += xd + ",";			
	}

//PO STWORZENIU LISTY WYSYLAMY JA DO WSZYSTKICH
	for (String dd : zbioraktywnychklientow)
{

try{
new DataOutputStream(((Socket) mapawszystkichklientow.get(dd)).getOutputStream()).writeUTF("@@@@@@"+ ids );
}

catch (Exception e) {
e.printStackTrace();
					}//KONIEC 1GO TRY I CATCH
} // KONIEC FOR..for (String dd : zbioraktywnychklientow)

}
catch (Exception e)
{
e.printStackTrace();
}
/// KONIEC 2GO TRY I CATCH

}// KONIEC public void run()
}   // KONIEC ..public Class Preparelistaklientow implements Runnable

private void grafika()//GUI....
{
frame = new JFrame();
frame.setBounds(200,200,900,600);
frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);;
frame.getContentPane().setLayout(null);
frame.getContentPane().setBackground(Color.RED);
frame.setTitle(" TESTOWY WIDOK SERWERA:  ");

serwerwyswietlacz = new JTextArea();
serwerwyswietlacz.setEditable(false);
serwerwyswietlacz.setBounds(12, 29, 489, 435);
frame.getContentPane().add(serwerwyswietlacz);
serwerwyswietlacz.setText(" to cwiczenia tylko ..rozruch serwera..");

listawszystkichklientow = new JList();
listawszystkichklientow.setBounds(526, 324, 218, 140);
frame.getContentPane().add(listawszystkichklientow);


activelistaklientow =  new JList();
activelistaklientow.setBounds(526, 78, 218, 156);
frame.getContentPane().add(activelistaklientow);

		JLabel lblNewLabel = new JLabel("All Usernames");
		lblNewLabel.setHorizontalAlignment(JLabel.RIGHT);
		lblNewLabel.setBounds(530, 295, 127, 16);
		frame.getContentPane().add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Active Users");
		lblNewLabel_1.setBounds(526, 53, 98, 23);
		frame.getContentPane().add(lblNewLabel_1);
}
	
	
}  //KONIEC KLASY SERWER