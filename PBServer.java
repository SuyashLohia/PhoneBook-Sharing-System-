import java.net.*;
import java.io.*;

public class PBServer {
	
	AccessControlManager ac_store;
	Phonebook pb;
	boolean autosave = true;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		PBServer cs = new PBServer();
		cs.go();
	}
	
	public void go() {		
		
		ac_store = new AccessControlManager();
		pb = new Phonebook(autosave);
//		if (autosave) {
//			Thread t1= new Thread (new AS());
//			t1.start();
//		}
		try {
			
			System.out.println("Server listening");
			ServerSocket ss = new ServerSocket(5000);

			while (true) {
				Socket s = ss.accept();
				
				System.out.println("Accepting connection");
				
				Thread t= new Thread(new ClientHandler(s));
				t.start();
				
//				ClientHandler ch = new ClientHandler(s);
//				ch.ch_go();
				
			}
		} catch (Exception e) { e.printStackTrace(); }
		
	}
	class AS implements Runnable{
		Boolean flag=false;
		public AS(Boolean flagt1) {
			// TODO Auto-generated constructor stub
			flag=flagt1;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (flag) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				pb.AutoupdatePBFile();
				System.out.println("File automatically updated after 5 secs ");
			}
		}
		
	}
	
	class ClientHandler implements Runnable{
		Boolean flagt1=true;
		OutputStream os;
		ObjectOutputStream oos;
		InputStream is;
		ObjectInputStream ois;
		
		public ClientHandler(Socket s) {		
			try {
				os = s.getOutputStream();
				oos = new ObjectOutputStream(os);
				is = s.getInputStream();
				ois = new ObjectInputStream(is);
			} catch (IOException e) {
				System.out.println("ClientHandler error");
				e.printStackTrace();
			}
			
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			this.ch_go();
			flagt1=false;
		}
		class AS implements Runnable{
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (flagt1) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					pb.AutoupdatePBFile();
					System.out.println("File automatically updated after 5 secs ");
				}
			}
			
		}
		
		public void ch_go() {	
			
			boolean more_msg = true;
			
			if (autosave) {
				Thread t1= new Thread (new AS());
				t1.start();
				}
			while ( more_msg ) {
				
				
				try {
					
					SPBMessage m = (SPBMessage) ois.readObject();
					System.out.println("Received msg:" + m);
					System.out.println("Recieved command: " + m.command);
					
					if ( m.command.equals("LOGIN") ) {
						if ( ac_store.verifyPassword(m.username, m.password) ) {
							m.reply_msg = "LOGIN success";
						}
						else 
							m.reply_msg = "LOGIN fail";
						
						oos.writeObject(m);
						continue;
					}
				
					String entry_name = m.entry.name;

					if ( ! ac_store.verifyPassword(m.username,m.password) )
					{
						m.reply_msg = "Incorrect password";
					}
					else if ( m.command.equals("GETBYNAME") )
					{
						System.out.println("GETBYNAME: " + entry_name);
						m.entry = null;
						if ( ac_store.checkReadPerm(m.username) )
						{
							String t= pb.entryExist1(entry_name);
							if(!t.isEmpty()){
								String[] temp= t.split(";");
								Integer i=Integer.parseInt(temp[0]);
								if ( i >=0 ) {
									m.entry = pb.getEntry(i);
									m.recno = i;
									if (temp.length==1) {
										m.reply_msg = "GETBYNAME success";
									}
									else {
										m.reply_msg ="GETBYNAME success: ";
										for (int j=0;j<temp.length-1;j++) {
											m.reply_msg+= temp[j] + " ,";
										}
									m.reply_msg+=temp[temp.length-1];
									}
								}
								else {
									m.reply_msg = "Phonebook entry does not exist";
								}
							}
							else {
								m.reply_msg = "Phonebook entry does not exist";
							}
						}
						else {
							m.reply_msg = "Invalid permission";
						}
					}
					else if ( m.command.equals("GETBYRECNO") )
					{
						// code to perform get by rec no.
						System.out.println("GETBYRECORDNO: " + m.recno);
						m.entry = null;
						if ( ac_store.checkReadPerm(m.username) )
						{
							int i = m.recno;
							if ( i >=0 && i<pb.entry_list.size()) 
							{
								m.entry = pb.getEntry(i);
								m.reply_msg = "GETBYRECNO success";
							}
							else
								m.reply_msg = "Phonebook entry does not exist";
						} else {
							m.reply_msg = "Invalid permission";
						}
					}
					else if ( m.command.equals("GETPREVBYRECNO") )
					{
						// code to perform get by rec no.
						System.out.println("GETPREVBYRECORDNO: " + m.recno);
						m.entry = null;
						if ( ac_store.checkReadPerm(m.username) )
						{
							int i = m.recno;
							if ( i >=1 && i<pb.entry_list.size()) 
							{
								m.recno=i-1;
								m.entry = pb.getEntry(i-1);
								m.reply_msg = "GETPREVBYRECNO success";
							}
							else
								m.reply_msg = "Phonebook entry does not exist";
						} else {
							m.reply_msg = "Invalid permission";
						}
					}
					else if ( m.command.equals("GETNEXTBYRECNO") )
					{
						// code to perform get by rec no.
						System.out.println("GETNEXTBYRECORDNO: " + m.recno);
						m.entry = null;
						if ( ac_store.checkReadPerm(m.username) )
						{
							int i = m.recno;
							
							if ( i >=0 && i<pb.entry_list.size()-1) 
							{
								m.recno=i+1;
								m.entry = pb.getEntry(i+1);
								m.reply_msg = "GETNEXTBYRECNO success";
							}
							else
								m.reply_msg = "Phonebook entry does not exist";
						} else {
							m.reply_msg = "Invalid permission";
						}
					}
					else  if ( m.command.equals("ADD") )
					{
						if ( ac_store.checkWritePerm(m.username) )
						{
							int i = pb.entryExist(m.entry.name);
							if ( i < 0 ) 
							{
								pb.addEntry(m.entry.name, m.entry);
								int recno = pb.entryExist(m.entry.name);
								m.reply_msg = "ADD success";
								m.recno = recno;
								pb.updatePBFile();
							}
						else
								m.reply_msg = "Phonebook entry exists";
						} else {
							m.reply_msg = "Invalid permission";
						}
					}
					else if ( m.command.equals("UPDATE") )
					{
						// code to update the Phonebook
						System.out.println("UPDATE: " + entry_name);
						if ( ac_store.checkWritePerm(m.username) )
						{
							int i = pb.entryExist(m.entry.name);
							if ( i > -1 ) 
							{
								pb.entry_list.set(i, m.entry);
								m.reply_msg = "UPDATE success";
								m.recno = i;
								pb.updatePBFile();
							}
						else
								m.reply_msg = "Invalid Phonebook entry ";
						} else {
							m.reply_msg = "Invalid permission";
						}
						
					}
					else if ( m.command.equals("DELETE") )
					{
						// code to delete the Phonebook entry
						System.out.println("DELETE: " + entry_name);
						if ( ac_store.checkWritePerm(m.username) )
						{
							int i = pb.entryExist(m.entry.name);
							if ( i > -1 ) 
							{
								pb.entry_list.get(i).deleted=true;
								m.reply_msg = "DELETE success";
								m.recno = i;
								pb.updatePBFile();
							}
						else
								m.reply_msg = "Invalid Phonebook entry ";
						} else {
							m.reply_msg = "Invalid permission";
						}
					}
					else if ( m.command.equals("LOGOFF") )
					{
						more_msg = false;
						oos.close();
						ois.close();
						return;
					}

					oos.writeObject(m);
				
				} catch ( Exception e ) {
					System.out.println("Exception in command processing");
					e.printStackTrace();
					return;
				}
					
			}		
											
		}


	}

}





