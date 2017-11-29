import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ListIterator;
import java.util.Vector;

public class Server implements Runnable {
	private final String pathscore;
	private final String pathgame;
	private final String pathfeedback;
	private final String pathchange;
	Vector<String> highscores;
	protected ServerSocket server;
	protected boolean run;
	private Thread thread;
	Window owner;
	private boolean changedHighscore;

	public Server(Window owner, int port) throws IOException {
			

		this.highscores = new Vector<String>();
		this.changedHighscore=true;
		this.pathfeedback = "C:\\Users\\Tim\\Documents\\Coding\\Server\\feedback.txt";
		this.pathscore = "C:\\Users\\Tim\\Documents\\Coding\\Server\\highscore.txt";
		this.pathgame = "C:\\Users\\Tim\\Documents\\Coding\\Heli\\heli.jar";
		this.pathchange = "C:\\Users\\Tim\\Documents\\Coding\\Server\\changelog.txt";
	
		this.owner = owner;

		start(port);
	}
	
	public void start(int port) throws IOException {
		
		this.server=new ServerSocket();
		this.server.setSoTimeout(3000);
		this.server.bind(new InetSocketAddress(port));

		
		this.run=true;
		this.thread = new Thread(this);
		this.thread.setName("SERVERGAME");
		this.thread.start();
		
	}

	public synchronized void startServing() {
		
		while (this.run) {
			Socket client = null;
			try {
				if (this.run) {
					client = this.server.accept();
				}
			}
			catch (SocketTimeoutException e){
				//ignore
				
			}catch (SocketException e) {
				
				
				e.printStackTrace();
				
				
			} catch (IOException e) {
				e.printStackTrace();
				this.owner.start.setEnabled(true);
				this.owner.stop.setEnabled(false);
				if (!this.server.isClosed()) {
					try {
						this.server.close();
					} catch (IOException e1) {
						e1.printStackTrace();
						owner.putLog("Server closing error");
					}

				}

			}

			try {
				if (client != null)
					handleConnection(client);
			} catch (IOException e) {
				e.printStackTrace();

			} finally {
				if (client != null)
					try {
						client.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
		try {
			server.close();
			owner.start.setEnabled(true);
			owner.putLog("Server is closed now");
			
			synchronized (owner) {
				owner.notify();		//you can go on now owner
			}
		} catch (IOException e) {
			owner.putLog("Server could not be closed");
			e.printStackTrace();
		}
	}

	private void updateClient(Socket client) throws IOException {
		OutputStream out = null;

		FileInputStream infile = null;
		try {
			out = client.getOutputStream();
			File sendfile = new File(this.pathgame);

			infile = new FileInputStream(sendfile);

			byte[] buffer = new byte[1024];

			myInt length = new myInt((int) sendfile.length());


			out.write(length.getBits(), 0, 31);		//XXX: find a better way to solve this

			out.write(length.getBits(), 0, 31);		//XXX: find a better way to solve

			out.flush();

			int bytesRead = 1;
			while (bytesRead > 0) {
				bytesRead = infile.read(buffer);

				if (bytesRead > 0) {
					out.write(buffer, 0, bytesRead);
				}

				if ((bytesRead < buffer.length) && (bytesRead > 0)) {
					out.flush();
				}

			}

			owner.putLog(client.getInetAddress() + " finished update successfully");
		} catch (SocketException e) {
			e.printStackTrace();

			owner.putLog(client.getInetAddress() + " has not finished update successfully (SocketException)");
		} catch (IOException e) {
			owner.putLog(client.getInetAddress() + " has not finished update successfully (IO-Exception)");
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
			if (infile != null)
				infile.close();
		}
	}

	private void receiveFeedback(Socket client) {
		BufferedReader in = null;
		BufferedWriter out = null;
		BufferedWriter intoFile = null;
		try {
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

			intoFile = new BufferedWriter(new FileWriter(new File(this.pathfeedback), true));

			String s = "";
			s = in.readLine();
			intoFile.write(owner.now.getcurr() + s + " (" + client.getInetAddress() + ") has send following feedback:");

			intoFile.newLine();

			owner.putLog(client.getInetAddress() + " (" + s + ") has sent feedback");
			while (!(s = in.readLine()).equals("{ende!}")) {
				intoFile.write('\t' + s);
				intoFile.newLine();
			}

			intoFile.newLine();

			out.write("true");
			out.newLine();
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (intoFile != null)
				try {
					intoFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	private void handleConnection(Socket client) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

		String order = in.readLine();
		if (!order.equals("startup")) {
			this.owner.getClass();
			if (order.equals(owner.version)) {
				order = in.readLine();
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(),"UTF-8"));
				if (order.equals("request Highscore")) {
					owner.putLog(client.getInetAddress() + " requests Highscore");

				
					getHighscorefromFile();  //TODO: change here
					sendHighscore(out);
					out.close();
				} else if (order.equals("save Highscore")) {
					String entry = in.readLine();

					owner.putLog(client.getInetAddress() + " saved Highscore (" + entry + ")");
					saveHighscore(entry);
					out.write("success");
					out.newLine();
					out.flush();
					out.close();
				} else if(order.equals("request Changelog")){
					owner.putLog(client.getInetAddress() + " requests Changelog");
					
					sendChangelog(getChangelogfromFile(),out);
				}
			} else {
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
				out.write("wrong Version");
				out.newLine();
				out.flush();
				out.close();
			}
		} else {
			order = in.readLine();
			if (order.equals("get Version")) {
				order = in.readLine();

				if (order.equals(owner.version)) {
					owner.putLog(client.getInetAddress() + " started game with right version");

					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
					out.write("right Version");
					out.newLine();
					out.flush();
					out.close();
				} else {
					owner.putLog(client.getInetAddress() + " started game with wrong version");
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

					out.write("wrong Version");
					out.newLine();
					out.flush();
					out.close();
				}
			} else if (order.equals("update")) {
				owner.putLog(client.getInetAddress() + " is updating");
				updateClient(client);
			} else if (order.equals("feedback")) {
				receiveFeedback(client);
			} else {
				owner.putLog(client.getInetAddress() + " wrong order");
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

				out.write("wrong Order");
				out.newLine();
				out.flush();
				out.close();
			}
		}

		in.close();
	}

	private void sendChangelog(Vector<String> changelog, BufferedWriter out){
		try {
			for (String s : changelog) {
				out.write(s);
				out.newLine();
			}
			
			out.flush();
		} catch (IOException e) {
			try {
				out.write("fail");
				out.newLine();
				out.flush();
			} catch (Exception e1) {
				
				e1.printStackTrace();
			}
			
			e.printStackTrace();
		}
	}
	
	private Vector<String> getChangelogfromFile() {
		BufferedReader outFile = null;
		Vector<String> changelog=new Vector<String>();
		try {
			outFile = new BufferedReader(new InputStreamReader(new FileInputStream(this.pathchange),"UTF-8"));

			
			String s;
			while ((s = outFile.readLine()) != null) {
				
				changelog.add(s);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (outFile != null)
				try {
					outFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		
		
		return changelog;
	}

	public void saveHighscore(String s) throws IOException {
		
		BufferedWriter inFile = null;
		String newScore = getScore(s);
		try {
			getHighscorefromFile();

			removeoldFile();
			File file = new File(this.pathscore.toString());

			inFile = new BufferedWriter(new FileWriter(file, true));

			file.createNewFile();

			String score = null;
			String entry = null;

			if (this.highscores.isEmpty()) {
				inFile.write(s);
				inFile.newLine();
			} else {
				int count = 1;
				boolean written = false;
				for (ListIterator<String> it = this.highscores.listIterator(); it.hasNext();) {
					if (count > 10) {
						break;
					}
					entry = (String) it.next();
					score = getScore(entry);
					count++;
					if (Integer.parseInt(newScore) <= Integer.parseInt(score)) {
						inFile.write(entry);
						inFile.newLine();
					} else {
						if (!written) {
							inFile.write(s);
							inFile.newLine();
							written = true;
						}
						inFile.write(entry);
						inFile.newLine();
					}

				}
				
				if ((!written) && (count <= 10)) {
					inFile.write(s);
					inFile.newLine();
				}
				
			}
			changedHighscore=true; //mark as changed here because old version will be send while saving highscore
			inFile.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inFile != null)
				inFile.close();
		}
	}

	
	public void getHighscorefromFile() throws IOException {
		if(changedHighscore){//if highscore changed after last call, open file
			BufferedReader outFile = null;
			try {
				outFile = new BufferedReader(new FileReader(this.pathscore));

				this.highscores.clear();
				String s;
				while ((s = outFile.readLine()) != null) {

					this.highscores.add(s);
				}
				changedHighscore=false;  //mark as unchanged
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (outFile != null)
					outFile.close();
			}
			
		}
		
	}

	public void sendHighscore(BufferedWriter out) throws IOException {
		try {
			for (ListIterator<String> it = this.highscores.listIterator(); it.hasNext();) {
				out.write((String) it.next());
				out.newLine();
			}

			out.flush();
		} catch (IOException e) {
			out.write("fail");
			out.newLine();
			out.flush();
			e.printStackTrace();
		}
	}

	private void removeoldFile() {
		File file = new File(this.pathscore.toString());
		file.delete();
		file = null;
	}

	public String getScore(String s) {
		int split = s.indexOf("//");
		int end = s.indexOf("///");
		if (split == -1) {
			return "-1";
		}
		if (end == -1) {
			return s.substring(split + 2);
		}

		return s.substring(split + 2, end);
	}

	public String getName(String s) {
		int split = s.indexOf("//");
		if (split == -1) {
			return "-1";
		}

		return s.substring(0, split);
	}

	public void run() {
		startServing();
	}


}
