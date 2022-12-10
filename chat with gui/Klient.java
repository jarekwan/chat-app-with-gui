package pakiet;

import java.net.Socket;
import java.awt.*;
import java.awt.Color;
import java.awt.Dimension;
import java.io.*;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;


public class Klient extends JFrame{


	private JFrame frame;
	private JTextField wspisKlienta;
	private JList listaAktywnych;	
	private JTextArea informacjeKlienta;
	private JButton PrzyciskClose;
	private JRadioButton PrzyciskIniiMulticast;
	private JRadioButton Przycisktyp1_broadcast;

	DataInputStream in;
	DataOutputStream out;
	DefaultListModel<String> md1;
	String nm, klientnm = "";


public Klient()
{
grafika();
}

public Klient(String nm, Socket s)
{
grafika(); 
		this.nm = nm;

try{			
frame.setTitle(" okno KlientA " + nm);
md1 = new DefaultListModel<String>();//do tej listy modeli za pomoca watku read bedziemy dodawac aktywni uzytkownicy

listaAktywnych.setModel(md1);
			in = new DataInputStream(s.getInputStream()); 
			out = new DataOutputStream(s.getOutputStream());
new Read().start(); //  METODA WATEK STWORZONY POZNIEJ SLUZY DO ZCZYTYWANYCH WYSYLANYCH Z SERWERA  aktywnych uzytkownikow ktory potem beda wyswietlani
}


catch(Exception ex)
{
ex.printStackTrace();
}//KONIEC CATCH

}// KONIEC Klient


class Read extends Thread
{
@Override

public void run()
{
	while(true){
	
try{


String m = in.readUTF();  

System.out.println(" THREAD READ INSIDEE: " + m + " YES");
if (m.contains("@@@@@@")) 
{
m = m.substring(6);// ubstring dzial tak ze od 6go dzielie tu wydziala od 6 czyli od 7go znaku
md1.clear();
String[] msgSt = m.split(",");
for(String ch:msgSt) 
{
if (!nm.equals(ch))
{
md1.addElement(ch); 
}

}  //KONIEC FOR
}  //KONIEC if (m.contains("@@@@@@"))

else{
informacjeKlienta.append("" + m + "\n"); 
}
}
catch (Exception e)
{
e.printStackTrace();
break;
}
	}

}
}  

	private void grafika()
	{
		frame = new JFrame();
		frame.setBounds(100, 100, 926, 705);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.getContentPane().setBackground(Color.BLUE);
		frame.setTitle("Client View");

		informacjeKlienta = new JTextArea();
		informacjeKlienta.setEditable(false);
		informacjeKlienta.setBounds(12, 25, 530, 495);
		frame.getContentPane().add(informacjeKlienta);

		wspisKlienta = new JTextField();
		wspisKlienta.setHorizontalAlignment(SwingConstants.LEFT);
		wspisKlienta.setBounds(12, 533, 530, 84);
		frame.getContentPane().add(wspisKlienta);
		wspisKlienta.setColumns(10);

         JButton clientSEND = new JButton(" SEND");
 		clientSEND.addActionListener(new ActionListener() { 
 		  	
  public void actionPerformed(ActionEvent e)
{
String teksttinformacji = wspisKlienta.getText();
if(teksttinformacji != null && !teksttinformacji.isEmpty())

{
try
{
String infodlaserwera;
String typ1 = "typ1_broadcast";
int flag = 0; 
if (PrzyciskIniiMulticast.isSelected()) 

{
typ1="multityp1";
List<String>listaklientow = listaAktywnych.getSelectedValuesList();
if (listaklientow.size() == 0) 
{
flag=1;// WYPADEK GDY NKOGO NIE ZAZNACZYLISMY
}
for(String usr: listaklientow)
{
if(klientnm.isEmpty())
{
klientnm += usr;
}

else{
klientnm +="," + usr;
}

} // KONIEC for(String usr: listaklientow)

infodlaserwera = typ1 + ":" + klientnm + ":" + teksttinformacji ;
} //KONIEC if (PrzyciskIniiMulticast.isSelected()) I TERAZ POWINIEN BYCH CATCH BO POWYZEJ IF JEST TRY
else
//GDY JEST typ1_broadcast ELSE W STOSUNKU DO if (PrzyciskIniiMulticast.isSelected()) 
{
infodlaserwera = typ1 + ":" + teksttinformacji ;

}
if (typ1.equalsIgnoreCase("multityp1"))
{
if(flag==1)
{
JOptionPane.showMessageDialog(frame, "No user selected");// POP UP MESSAGE SKLADA SIE Z PARENT COMPONENT I SAMEGO MESSAGE
}
else 
//GDY JEDNAK KOGOS WYBRALISMY
{
out.writeUTF(infodlaserwera);
wspisKlienta.setText("");
informacjeKlienta.append("< You sent msg to " + klientnm + ">" + teksttinformacji + "\n"); 
}
} 

else
//GDY NIE JEST MULTItyp1 ALE JEST typ1_broadcast
{
out.writeUTF(infodlaserwera);
wspisKlienta.setText("");
informacjeKlienta.append("< You sent msg to All >" + teksttinformacji + "\n");

}//KONIEC IF ELSE  if (typ1.equalsIgnoreCase("multityp1")
klientnm = "";

} //KONIEC TRY
catch(Exception ex)
{
JOptionPane.showMessageDialog(frame, "User does not exist anymore."); // if user doesn't exist then show message
}

}  //KONIEC if(teksttinformacji != null && !teksttinformacji.isEmpty)
} //KONIEC   public void actionPerformed(ActionEvent e)
});
// KONIEC  clientSEND.addActionListener(new ActionListener()	

//INNE PRZYCISKI KTORE ROWNIEZ MAJA ZASTOSOWANIE POWYZEJ	
clientSEND.setBounds(554, 533, 137, 84);
frame.getContentPane().add(clientSEND);		
		
listaAktywnych = new JList();
listaAktywnych.setToolTipText("aktywni uzytkownicy");// dziwne narzedze TOOLTIPTEXT 	 ponoac jak najezdzasz muszka to robi sie opoup text wpisany	
listaAktywnych.setBounds(554, 63, 327, 457);
frame.getContentPane().add(listaAktywnych);

PrzyciskClose = new JButton("zamknij");
PrzyciskClose.addActionListener(new ActionListener()
// KILL PROCESS
{
public void actionPerformed(ActionEvent e)
{
try{
out.writeUTF("exit");
informacjeKlienta.append(" disconnected .. ");
frame.dispose(); // close the frame 

}
catch(IOException ex)
{
	ex.printStackTrace();
}
}



});	
PrzyciskClose.setBounds(703, 533, 193, 84);
frame.getContentPane().add(PrzyciskClose);

		PrzyciskIniiMulticast = new JRadioButton("Multi i Uni cast");
		PrzyciskIniiMulticast.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				listaAktywnych.setEnabled(true);
			}
		});
		PrzyciskIniiMulticast.setSelected(true);
		PrzyciskIniiMulticast.setBounds(682, 24, 72, 25);
		frame.getContentPane().add(PrzyciskIniiMulticast);
		
		

		Przycisktyp1_broadcast = new JRadioButton("typ1_broadcast");
		Przycisktyp1_broadcast.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				listaAktywnych.setEnabled(false);
			}
		});
		Przycisktyp1_broadcast.setBounds(774, 24, 107, 25);
		frame.getContentPane().add(Przycisktyp1_broadcast);

		ButtonGroup Przyciskgrupa = new ButtonGroup();
		Przyciskgrupa.add(PrzyciskIniiMulticast);
		Przyciskgrupa.add(Przycisktyp1_broadcast);



JLabel Label1 = new JLabel("aktywni uzytkownicy");
Label1.setHorizontalAlignment(JLabel.LEFT);
Label1.setBounds(559, 43, 95, 16);
frame.getContentPane().add(Label1);

	
frame.setVisible(true);		
	}  //KONIEC 


}




