package pakiet;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;


public class Login
{

private JFrame frame;
private JTextField klientnm;
private int port = 9999;



public static void main(String[] args) { 
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login window = new Login();
					window.frame.setVisible(true);
				} //KONIEC TRY
catch (Exception e) {
					e.printStackTrace();
				}//KONIEC CATCH
			}// KONIEC RUN
		});//KONIEC EVENTQUEUE
	}//KONIEC MAIN


public Login ()
{
grafika();
} //KONIE CLOGIN METODY KTORA URUCHAMIA GRAFIKE
//UWA RAZ TWORZE VOID RAZ NIE METODY..VNIE POTRAFIE TEGO WYJASNIC TAK DO KONCA DLACZEGO LOGI NI JEST VOID A WIELE INNYCH JEST

   private void grafika()
{

frame = new JFrame();
frame.setBounds(100, 100, 619, 342);
frame.getContentPane().setLayout(null);
frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
frame.getContentPane().setBackground(Color.BLUE);
frame.setTitle(" rejestracja klienta.. ");



klientnm = new JTextField();
klientnm.setEditable(true);
klientnm.setBounds(207, 50, 276, 61);
frame.getContentPane().add(klientnm);
klientnm.setColumns(10);

		
		

JButton PrzyciskKlienta = new JButton("polaczenie");
PrzyciskKlienta.addActionListener(new ActionListener(){
public void actionPerformed(ActionEvent e)
{
try
{
String username = klientnm.getText();// username entered by user
Socket client = new Socket("localhost", port);


					DataInputStream in = new DataInputStream(client.getInputStream()); 
					DataOutputStream out = new DataOutputStream(client.getOutputStream());
					out.writeUTF(username); // send username to the output stream

new Klient(username, client);

frame.dispose();//zamkniecie ale nie tak drastyczne jak exit

}//KONIEC TRY

catch(Exception ex){
ex.printStackTrace();

}//KONIEC CATCH


} // KONIEC ACTIONPEFORMED


}) ;
//KONIEC ACTION LISTENER
//PONIZEJ DOKANCZANIE USTAWIEN BUTTON
PrzyciskKlienta.setFont(new Font("Serif", Font.ITALIC, 24));
PrzyciskKlienta.setBounds(207, 139, 132, 61);
frame.getContentPane().add(PrzyciskKlienta);

JLabel NLAB = new JLabel("nazwa usera ");
NLAB.setFont(new Font("Serif", Font.ITALIC, 24));
NLAB.setBounds(44, 55, 132, 47);
NLAB.setHorizontalAlignment(JLabel.CENTER);
frame.getContentPane().add(NLAB);


}


}