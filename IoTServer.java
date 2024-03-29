import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;


public class IoTServer {

	public static Auth authenticateUser(String username, String password) {
        String fileName = "users.txt";
        Map<String, String> users = new HashMap<>();
        System.out.println("entrei na função authenticateUser. server 21");

        try {
            // Read the existing user data from the text file
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }
            reader.close();

            // Check if the username exists
            if (users.containsKey(username)) {
                String storedPassword = users.get(username);
                // Check if the password matches
                if (storedPassword.equals(password)) {
                    return Auth.OK_USER; // Authentication successful
                } else {
                    return Auth.PASSWORD_NO_MATCH; // Password doesn't match
                }
            } else {
                // Add the new user to the text file
                FileWriter writer = new FileWriter(fileName, true);
                writer.write(username + ":" + password + "\n");
                writer.close();
                return Auth.NEW_USER; // New user added successfully
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Auth.ERROR; // Error occurred
        }
    }
	
    
    public static void main(String[] args) {
        System.out.println("servidor: main");
		IoTServer server = new IoTServer();
        int port;
        if (args.length==0) {
            port = 12345;
        } else {
            port = Integer.parseInt(args[0]);
        }

		server.startServer(port);
    }

    public void startServer (int port){
		ServerSocket sSoc = null;


		try {
			sSoc = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
         
		while(true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
		    }
		    catch (IOException e) {
		        e.printStackTrace();
		    }
		    
		}
		//sSoc.close();
	}

    //Threads utilizadas para comunicacao com os clientes
	class ServerThread extends Thread {

		private Socket socket = null;

		ServerThread(Socket inSoc) {
			socket = inSoc;
			System.out.println("thread do server para cada cliente");
		}
 
		public void run(){
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

				String user_id = null;
				String passwd = null;
			
				try {
					user_id = (String)inStream.readObject();
					passwd = (String)inStream.readObject();
                    System.out.println(user_id);
					System.out.println("thread: depois de receber a password e o user_id");
				}catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}
 			
				// TODO AUTENTICAR
				// ler e escrever no ficheiro users.json
                Auth autenticado = authenticateUser(user_id,passwd);

				outStream.writeObject(autenticado);
				//outStream.writeBoolean(autenticado);
				System.out.println(autenticado);

                String comando;
                boolean loop = true;
                while (loop) {
                    comando = (String)inStream.readObject();
                    System.out.print(user_id + ": ");
					System.out.println(comando);
                    String[] comandoSplit = comando.split(" ");

                    // TODO PROCESSAR COMANDOS AQUI

                    //if (comandoSplit[0].equals("ADD")) {}
                }

				outStream.close();
				inStream.close();
 			
				socket.close();

			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			} 
		}
	}

}
