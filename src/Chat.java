import java.util.ArrayList;

import org.json.JSONObject;
import java.io.*;

//import com.stanford_nlp.model.*;

public class Chat {
	//public JSONObject json;
	public ArrayList<String> authors = new ArrayList<String>();
	public ArrayList<Long> authorIDs = new ArrayList<Long>();
	public ArrayList<String> messages = new ArrayList<String>();
	
	public Chat(ArrayList<String> arrayList) throws FileNotFoundException {
		 /* Constructing String Builder to
        append the string into the html */
		//System.out.println(arrayList.get(0));
        StringBuilder html = new StringBuilder();
        int x = 0;
        for (String e : arrayList) {
        	html.append(e);
        	html.append("\n");
        	//System.out.println(e);
        	if (e.indexOf("<div class=\"chatlog__messages\">") != -1 && x == 0) {
        		x = 1;
        	}
        	if (x == 1 && e.indexOf("data-user-id=\"") != -1) {
        		authors.add(e.substring(e.indexOf("title=\"")+7, e.indexOf("\"", e.indexOf("title=\"")+8)));
        		authorIDs.add(Long.parseLong(e.substring(e.indexOf("data-user-id=\"")+14, e.indexOf("\"", e.indexOf("data-user-id=\"")+15))));
        		//System.out.println(authors.get(authors.size()-1) + "    " + authorIDs.get(authorIDs.size()-1));
        	}
        	if (x == 1 && e.indexOf("<span class=\"preserve-whitespace\">") != -1) {
        		x = 2;
        		try {
        			if (e.indexOf("</span>", e.indexOf("<span class=\"preserve-whitespace\">")+35) == -1) {
        				messages.add(fixText(e.substring(e.indexOf("<span class=\"preserve-whitespace\">")+34)));
        				x = 3;
        			} else {
        				messages.add(fixText(e.substring(e.indexOf("<span class=\"preserve-whitespace\">")+34, e.indexOf("</span>", e.indexOf("<span class=\"preserve-whitespace\">")+35))));
        			}
        		} catch (Exception f) {
        			System.out.println(e);
        			f.printStackTrace();
        		}
        		//System.out.println(messages.get(messages.size()-1));
        	}
        	else if (x == 3) {
        		if (e.indexOf("</span>") == -1) {
        			messages.set(messages.size()-1, fixText(messages.get(messages.size()-1) + "\n" + e));
        		} else {
        			messages.set(messages.size()-1, fixText(messages.get(messages.size()-1) + "\n" + e.substring(0, e.indexOf("</span>"))));
        		}
        	}
        	if (e.indexOf("<div class=\"chatlog__message-group\">") != -1) {
        		if (x == 1)
        			messages.add(null);
        		x = 0;
        		//System.out.println();
        	}
        	
        }
	}
	
	// Format the message (very ghetto html parser lmao)
	public String fixText(String s) {
		String result = s.replace("&#39;", "'");
		result = result.replace("&quot;", "\"");
		result = result.replace("<a href=\"", "");
		result = result.replace("</a>", "");
		result = result.replace("<em>", "*");
		result = result.replace("</em>", "*");
		result = result.replace("<s>", "~~");
		result = result.replace("</s>", "~~");
		result = result.replace("<strong>", "***");
		result = result.replace("</strong>", "***");
		if (result.indexOf("</div>") != -1) {
			result = result.substring(0, result.indexOf("</div>"));
		}
		if (result.indexOf("<div class=") != -1) {
			result = result.substring(0, result.indexOf("<div class="));
		}
		if (result.indexOf("<span class=\"spoiler-text spoiler-text--hidden\" onclick=\"showSpoiler(event, this)\">") != -1) {
			result = "||" + result.replace("<span class=\"spoiler-text spoiler-text--hidden\" onclick=\"showSpoiler(event, this)\">", "") + "||";
		}
		if (result.indexOf("https://") != -1 && result.indexOf("\">", result.indexOf("https://")) > result.indexOf("https://")) {
			result = result.replace(result.substring(result.indexOf("https://"), result.indexOf("\">", result.indexOf("https://"))+1), "");
		}
		if (result.indexOf("<span class=\"mention\" title=") != -1) {
			long userID = -1;
			String username = result.substring(result.indexOf("title=")+7, result.indexOf("\"", result.indexOf("title=")+8));
			try {
				userID = Test.api.getCachedUserByDiscriminatedNameIgnoreCase(username).get().getId();
			} catch (Exception e) {
				for (String[] f : Constants.userIDs) {
					if (username.equalsIgnoreCase(f[0])) {
						userID = Long.parseLong(f[1]);
						break;
					}
				}
				if (userID == -1)
					System.out.println("Unknown User ==> " + username);
			}
			String mention = userID != -1 ? "<@!" + userID + ">" : "@" + username;
			result = result.replace(result.substring(result.indexOf("<span class=\"mention\" title="), result.indexOf("</span>") == -1 ? result.length() : result.indexOf("</span>")+7), mention + " ");
		}
		
		result = result.replace("<span class=\"mention\">", "");
		
		return result;
	}
	
	public double[] getMostSimilarMessage(String m) {
		
		double simScore = -1;
		int index = -1;
		
		for (int i = 0; i < messages.size(); i++) {
			double sim = StringSimilarity.similarity(m, messages.get(i)) + 0.1*(Math.random()-0.5);
			if (sim > simScore) {
				simScore = sim;
				index = i;
				if (simScore > 0.96)
					return new double[] {index, simScore};
			}
		}
		
		return new double[] {index, simScore};
	}
	
	int statusMin = 5;
	int statusMax = 130;
	
	public String getStatus(int indexInput) {
		String result = messages.get(indexInput);
		
		for (int i = indexInput; i < messages.size() && i < indexInput + 10; i++) {
			String m = messages.get(i);
			if (m.length() > statusMin && m.length() < statusMax) {
				result = m;
				if (Math.random() > 0.7)
					return m;
			}
		}
		
		return result;
	}
	
	public String getReply(double simInput, int indexInput) {
		double simScore = -1;
		int index = -1;
		String result = messages.get(indexInput);
		//System.out.println("Yeet");
		if (Math.random() < simInput*1.1) {
			//System.out.println("Checking for reply");
			int x = 0;
			for (int i = indexInput; i < messages.size() && i < indexInput+16 && x < 6; i++) {
				//System.out.println("Looking for reply");
				if (authorIDs.get(indexInput) != authorIDs.get(i)) {
					x++;
					double sim = StringSimilarity.similarity(messages.get(indexInput), messages.get(i)) + 0.2*(Math.random()-0.5);
					if (sim > simScore) {
						//System.out.println("Comparing reply");
						simScore = sim;
						result = messages.get(i);
					}
				}
			}
			
		}
		
		if (result.length() > 1)
			result = result.substring(0, 1).toUpperCase() + result.substring(1);
		return result;
	}
}
