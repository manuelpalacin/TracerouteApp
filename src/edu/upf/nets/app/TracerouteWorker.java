package edu.upf.nets.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

public class TracerouteWorker extends SwingWorker<String, String> {

	
	private static final Logger log = Logger
			.getLogger(TracerouteWorker.class.getName());
	
	private final String destination;
	private final String mercuryServerURL;
	private final JTextArea taTraceroute;

	
	/**
	 * Constructor
	 * We pass it the textarea component to be updated
	 * @param destination
	 * @param mercuryServerURL
	 * @param taTraceroute
	 */
    public TracerouteWorker(String destination, String mercuryServerURL, JTextArea taTraceroute) {
    	this.destination = destination;
    	this.mercuryServerURL = mercuryServerURL; //http://mercury.upf.edu/mercury/api/traceroute/uploadTrace
        this.taTraceroute = taTraceroute;
    }
	
    /**
     * SwingWorker demanding task is executed here
     * 
     */
	@Override
	protected String doInBackground() throws Exception {

		traceroute(destination);
		
		return null;
	}
	
    /**
     * Task done, SwingWorker calls this method in the event thread.
     * Here we update the XXXX component
     */
    @Override
    protected void done() {
    	
    }
	
    /**
     * SwingWorker calls this method in the events thread when 
     * we call the publish method and it passes the same parameters.
     * We use it for updating the textarea console
     */
    @Override
    protected void process(List<String> texts) {
        //print("process() is in thread " + Thread.currentThread().getName());
        taTraceroute.append("[" + new Date() + "] " + "[" + destination + "]" + texts.get(0));
    }

	private void traceroute(String destination){
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.indexOf("windows") != -1) {
			// -d to avoid name resolution
			String[] command = { "tracert", "-d", "-w", "500", "-h", "30", destination }; 
			exec(command);
		} else if (osName.indexOf("mac os x") != -1) {
			// -n to avoid name resolution, -w to set waittime, -m to set the max TTL
			String[] command = { "traceroute", "-n", "-w", "1", "-m", "30", destination };
			exec(command);
		} else {
			// -n to avoid name resolution
			String[] command = { "traceroute", "-n", "-w", "1", "-m", "30", destination }; 
			exec(command);
		}
		print(osName);
	}
	
	private void exec(String[] command) {
		
		try {

			ProcessBuilder builder = new ProcessBuilder(command);
			final Process process = builder.start();
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			List<String> lines = new ArrayList<String>();
			
			while ((line = br.readLine()) != null) {
				print(line);
				//taTraceroute.append(line + "\n");
				//taTraceroute.update(taTraceroute.getGraphics());
				publish(line + "\n");
				
				lines.add(line);
			}

			int retval = process.waitFor();
			print("Retval: " + retval);

			// Patterns
			String patternHop = "(^\\s+\\d{1,2}|^\\d{1,2})";
			String patternIp = "([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})";

			Matcher matcherReply;
			String firstLine;
			String traceroute = "{\"destination\":\"" + command[6]
					+ "\",\"hops\":[]}";
			// When traceroute process finishes, we analyze the output line per
			// line
			if (retval == 0) {
				// We obtain the destination IP from the first line
				if ((firstLine = lines.get(0)) != null) {
					// Workaround for Windows. Data starts at second line
					if ((firstLine = lines.get(0)).equals("")) {
						if ((firstLine = lines.get(1)) != null) {
							if ((matcherReply = Pattern.compile(patternIp)
									.matcher(firstLine)).find()) {
								firstLine = matcherReply.group(0);
							}
						}
					} else {
						if ((matcherReply = Pattern.compile(patternIp).matcher(
								firstLine)).find()) {
							firstLine = matcherReply.group(0);
						}
					}
				}
				// We process each hop
				String hops = "";
				for (String lineAux : lines) {
					String hop;
					if ((matcherReply = Pattern.compile(patternHop).matcher(
							lineAux)).find()) {
						String hopId = matcherReply.group(0).trim();
						// hop.setId(matcherReply.group(0).trim()); //We add the
						// hop Id
						if ((matcherReply = Pattern.compile(patternIp).matcher(
								lineAux)).find()) {
							hop = "{\"id\":\"" + hopId + "\",\"ip\":\""
									+ matcherReply.group(0) + "\" }";
							// hop.setIp(matcherReply.group(0)); //We add the
							// hop Ip
						} else {
							hop = "{\"id\":\"" + hopId
									+ "\",\"ip\":\"destination unreachable\" }";
							// hop.setIp("host unreachable"); //We add the hop
							// Ip unreachable
						}
						// traceroute.addHops(hop);
						if (hops.equals("")) {
							hops = hop;
						} else {
							hops = hops + "," + hop;
						}
					}
				}
				
				String myIp = InetAddress.getLocalHost().getHostAddress();
				traceroute = "{\"myIp\":\"" + myIp
						+ "\",\"destination\":\"" + command[6]
						+ "\",\"ip\":\"" + firstLine + "\",\"hops\":[" + hops
						+ "]}";
			}

			//We store a file and then we send the data
			saveData(traceroute, destination);
			String result = postData(traceroute);
			print(result);
			publish(result + "\n\n");


		} catch (Exception exception) {
			exception.printStackTrace();
		}

	}
	
	
	private void saveData(String data, String destination){
		try {
			 
 
			File file = new File(destination+"-"+new Date().getTime()+".json");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(data);
			bw.close();
 
			print("Traceroute data stored in >>> "+file.getCanonicalPath());
 
		} catch (IOException e) {
			e.printStackTrace();
			print("Problems writing the file");
		}
	}
	
	private String postData(String data) {

		String result;

		try {
			URL url = new URL(mercuryServerURL);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoInput(true);
			connection.setDoOutput(true);

			connection.setRequestProperty("Content-Type",
					"application/json;charset=UTF-8");

			DataOutputStream wr = new DataOutputStream(
					connection.getOutputStream());
			wr.write(data.getBytes("UTF-8"));
			wr.flush();
			wr.close();

			int status = connection.getResponseCode();
			print("Status: " + status);
			if ((status == 200) || (status == 201)) {
				print("Added traceroute data!");
				result = "Added traceroute data to Mercury. Thanks for participating!";
			} else {
				print("Problems adding entry. Server response status: "
						+ status);
				result = "Problems adding traceroute data to Mercury server. Try again later. Server response status: "
						+ status;
			}

			connection.disconnect();

		} catch (Exception e) {
			result = "Problems adding entry. i.e. Mercury server not reachable";
		}
		return result;
	}
	
	
	
	
    private void print(String msg, Object... args) {
        log.info(String.format(msg, args));
    }


}

