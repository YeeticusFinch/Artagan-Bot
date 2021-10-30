import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.javacord.api.*;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.json.JSONObject;
import org.javacord.api.entity.channel.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;

public class Test {
	
	public static ArrayList<Long> allowedChannels = new ArrayList<Long>();
	public static JSONObject jsonObject;
	
	public static ArrayList<Chat> chats = new ArrayList<Chat>();
	public static DiscordApi api;
	
	public static ArrayList<User> users = new ArrayList<User>();
	
	public static ArrayList<Server> servers = new ArrayList<Server>();

	public static void main(String[] args) {
		// Insert your bot's token here
        String token = Constants.authToken;
        
        api = new DiscordApiBuilder().setToken(token).login().join();

        api.updateActivity("Starting up...");
        
        for (String[] f : Constants.userIDs2) {
        	try {
				users.add(api.getUserById(f[1]).get());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        File folder = new File("chats");
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
        	  if (listOfFiles[i].isFile()) {
        	    System.out.println("File " + listOfFiles[i].getName());
        	    try {
					chats.add(new Chat(FileIO.read("chats" + FileIO.fileSep + listOfFiles[i].getName())));
					//break;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	  } else if (listOfFiles[i].isDirectory()) {
        	    System.out.println("Directory " + listOfFiles[i].getName());
        	  }
        }
        
        for (long e : Constants.savedServers)
        	allowedChannels.add(e); 
        
        // Add a listener to respond to texts
        api.addMessageCreateListener(event -> {
        	if (event.getMessageContent().length() > 0 && (event.getMessageContent().charAt(0) == '~' || event.getChannel() instanceof PrivateChannel || isAllowedChannel(event.getChannel().getId()))) {
        		handleText(event);
        	}
        });

        // Print the invite url of your bot
        String inviteLink = api.createBotInvite();
        System.out.println("You can invite the bot by using the following url: " + inviteLink.substring(0, inviteLink.length()-1) + Constants.perms);
        api.getChannelById(Constants.adminChannel).ifPresent(x->x.asTextChannel().ifPresent(y->y.sendMessage("I'm alive")));
        
        newStatus();
	
		while (true) {
			try {
				TimeUnit.MINUTES.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			LocalDateTime now = LocalDateTime.now();
			if (now.getHour() > 8 && users.size() > 0) {
				if (Math.random() < 0.02) {
					for (int j = 0; j < 2; j++) {
						startConvo();
					}
				}
			}
			//System.out.println(now.getHour());
		}
	}
	
	// Choose from a hardcoded status
	public static void newStatus() {
		if (Math.random() > 0.7) {
			switch ((int)(Math.random() * 4)) {
				case 0:
					api.updateActivity(ActivityType.LISTENING, new String[] {"idiots", "bullshit", "It's Tought to be a God", "I Can't Decide", "sounds of the Feywild", "your mom", "Scanlan"}[(int)(7*Math.random())]);
					break;
				case 1:
					api.updateActivity(ActivityType.PLAYING, new String[] {"with people's feelings", "with sanity", "with beans", "God", "truth or dare", "Dragonchess", "Three-Dragon Ante"}[(int)(7*Math.random())]);
					break;
				case 2:
					api.updateActivity(ActivityType.STREAMING, new String[] {"my way into the material plane", "pure insanity", "beans", "Critical Role", "myself into your reality", "Dragonchess tournaments", "Three-Dragon Ante tournaments"}[(int)(7*Math.random())]);
					break;
				case 3:
					api.updateActivity(ActivityType.WATCHING, new String[] {"out for you", "the chroma conclave", "the beans", "Critical Role", "the chaos unfold", "Dragonchess tournaments", "Three-Dragon Ante tournaments"}[(int)(7*Math.random())]);
					break;
			}
		} else {
			String yeet = Constants.statusIdeas[(int)(Math.random()*Constants.statusIdeas.length)];
			System.out.println(yeet);
			api.updateActivity(yeet);
		}
	}
	
	// Sends a DM to a random person in a server this bot has access to
	public static void startConvo() {
		try {
			PrivateChannel channel = null;
			User randUser = users.get((int)(Math.random()*users.size()));
			try {
				channel = randUser.getPrivateChannel().get();
			} catch (Exception e) {}
			if (channel == null) {
				try {
					channel = randUser.openPrivateChannel().get();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			String messageToSend = "";
			if (channel != null) {
				while (messageToSend.length() < 25 && messageToSend.indexOf("<img") == -1) {
					Chat chatToSend = chats.get((int)(Math.random()*chats.size()));
					messageToSend = chatToSend.messages.get((int)(Math.random()*chatToSend.messages.size()));
					if (messageToSend == null) {
						messageToSend = "";
					}
				}
				String messageToSend2 = messageToSend + "";
				try {
					api.getChannelById(Constants.adminChannel).ifPresent(x->x.asTextChannel().get().sendMessage("Initiating Conversation with " + randUser.getName() + " by sending --> " + messageToSend2));
				} catch (Exception e) {
					
				}
				channel.sendMessage(messageToSend2);
			}
		} catch (Exception e) {}
	}
	
	//Add all members from the server
	public static void scanServer(Server server) {
		for (Server e : servers) {
			if (e.getId() == server.getId())
				break;
		}
		servers.add(server);
		Collection<User> members = server.getMembers();
		for (User e : members) {
			boolean userPresent = false;
			for (User f : users) {
				if (f.getId() == e.getId()) {
					userPresent = true;
				}
			}
			if (!userPresent) {
				users.add(e);
			}
		}
	}
	
	public static void handleText(MessageCreateEvent event) {
		if (event.getMessageAuthor().isYourself())
			return;
		boolean userSaved = false;
		for (User e : users) 
			if (e.getId() == event.getMessageAuthor().getId())
				userSaved = true;
		try {
			if (!userSaved)
				users.add(event.getMessageAuthor().asUser().get());
		} catch (Exception e) {}
		try {
			scanServer(event.getServer().get());
		} catch (Exception e) {}
		
		String message = event.getMessageContent();
		System.out.println("Message = " + message);
		if (message.charAt(0) == '~') { // If its a command
			System.out.println("Command");
			message = message.substring(1);
			if (isOne(message, new String[]{"allow", "enter", "portal", "enable", "summon", "invite"})) { // invite to current channel
				for (long e : allowedChannels) {
					if (e == event.getChannel().getId()) {
						event.getChannel().sendMessage("I already have this channel added");
						return;
					}
				}
				event.getChannel().sendMessage(new String[] {"Delightfully foolish, I love it.", "Travel safe.  Stay alive.  And visit if you get lonely.", "What strangeness does this world offer?", "Nothing happens for a reason; it's absolute fucking chaos."}[(int)(4*Math.random())]);
				allowedChannels.add(event.getChannel().getId());
				api.getChannelById(Constants.adminChannel).ifPresent(x->x.asTextChannel().ifPresent(y->y.sendMessage("-- Allowed Channels --")));
				System.out.println("-- Allowed Channels --");
				for (long e : allowedChannels) {
					api.getChannelById(Constants.adminChannel).ifPresent(x->x.asTextChannel().ifPresent(y->y.sendMessage(""+e)));
					System.out.println(e);
				}
				return;
			} else if (isOne(message, new String[]{"leave", "bye", "fuckoff", "gtfo", "stfu", "ban", "run", "disable", "kick", "bounce", "exit"})) { // kick from current channel
				boolean temp = false;
				for (long e : allowedChannels) {
					if (e == event.getChannel().getId()) {
						temp = true;
					}
				}
				if (!temp) {
					event.getChannel().sendMessage("I've never seen this channel in my life");
					return;
				}
				event.getChannel().sendMessage(new String[] {"Farewell. Travel safe. Stay alive. And visit if you get lonely.", "Goodbye boring world.", "Farewell comrade.", "I'm outa here!"}[(int)(4*Math.random())]);
				allowedChannels.remove(event.getChannel().getId());
				api.getChannelById(Constants.adminChannel).ifPresent(x->x.asTextChannel().ifPresent(y->y.sendMessage("-- Allowed Channels --")));
				System.out.println("-- Allowed Channels --");
				for (long e : allowedChannels) {
					api.getChannelById(Constants.adminChannel).ifPresent(x->x.asTextChannel().ifPresent(y->y.sendMessage(""+e)));
					System.out.println(e);
				}
				return;
			} else if (message.length() > 4 && message.substring(0, 4).equalsIgnoreCase("add ")) { // Add a channel by ID
				long newChannel = -1;
				try {
					newChannel = Long.parseLong(message.substring(4));
				} catch (Exception e) {
					event.getChannel().sendMessage("bruh " + message.substring(4) + " is not a channel ID, you must give give me the channel ID, it's supposed to look like a number, this command is for adding channels by ID");
					return;
				}
				for (long e : allowedChannels) {
					if (e == newChannel) {
						event.getChannel().sendMessage("I already have this channel added");
						return;
					}
				}
				event.getChannel().sendMessage(new String[] {"Adding channel to my repetoir", "Oh cool I'll hop right in!", "Another channel for me to join? Great", "Eyo thanks for the invite!"}[(int)(4*Math.random())]);
				allowedChannels.add(newChannel);
				api.getChannelById(Constants.adminChannel).ifPresent(x->x.asTextChannel().ifPresent(y->y.sendMessage("-- Allowed Channels --")));
				System.out.println("-- Allowed Channels --");
				for (long e : allowedChannels) {
					api.getChannelById(Constants.adminChannel).ifPresent(x->x.asTextChannel().ifPresent(y->y.sendMessage(""+e)));
					System.out.println(e);
				}
				return;
			} else if (message.length() > 5 && message.substring(0, 5).equalsIgnoreCase("send ")) { // Sends this exact message
				long newChannel = -1;
				try {
					newChannel = Long.parseLong(message.substring(5, message.indexOf(" ", 6)));
				} catch (Exception e) {
					event.getChannel().sendMessage("bruh " + message.substring(5, message.indexOf(" ", 6)) + " is not a channel ID, you must give me the channel ID, it's supposed to look like a number, this command is for sending text to a specific channel");
					return;
				}
				String messageToSend = message.substring(message.indexOf(" ", 6)+1);
				api.getChannelById(newChannel).ifPresent(x->x.asTextChannel().ifPresent(y->y.sendMessage(messageToSend)));
				event.getChannel().sendMessage("Aight boss I sent the message!");
				return;
			} else if (message.length() > 12 && message.substring(0, 12).equalsIgnoreCase("send_similar")) { // Finds a similar string in my message history to send
				long newChannel = -1;
				try {
					newChannel = Long.parseLong(message.substring(13, message.indexOf(" ", 14)));
				} catch (Exception e) {
					event.getChannel().sendMessage("bruh " + message.substring(13, message.indexOf(" ", 14)) + " is not a channel ID, you must give me the channel ID, it's supposed to look like a number, this command is for sending text to a specific channel");
					return;
				}
				
				int mStart = message.indexOf(" ", 14)+1;
				String mention = "";
				if (message.indexOf("<@!") != -1) {
					System.out.println("Found a mention");
					mention = message.substring(message.indexOf("<@!"), message.indexOf(" ", message.indexOf("<@!")+3));
					mStart =  message.indexOf(" ", message.indexOf("<@!")+3);
				}
				
				double simScore = -1;
				int index = -1;
				int chatIndex = -1;
				for (int i = 0; i < chats.size(); i++) {
					double[] yeet = chats.get(i).getMostSimilarMessage(message.substring(mStart));
					if (yeet[1] > simScore) {
						index = (int)yeet[0];
						simScore = yeet[1];
						chatIndex = i;
					}
				}
				String messageToSend;
				if (chatIndex == -1 || index == -1) {
					messageToSend = (mention.length() > 0 ? new String[] {"Yo ", "Hey ", "Guess what "}[(int)(Math.random()*3)] + mention + ", " : "") + message.substring(message.indexOf(" ", 14)+1);
					api.getChannelById(newChannel).ifPresent(x->x.asTextChannel().ifPresent(y->y.sendMessage(messageToSend)));
				} else {
					messageToSend = (mention.length() > 0 ? new String[] {"Yo ", "Hey ", "Guess what "}[(int)(Math.random()*3)] + mention + ", " : "") + chats.get(chatIndex).messages.get(index);
					System.out.println("Sending message --> " + messageToSend);
					api.getChannelById(newChannel).ifPresent(x->x.asTextChannel().ifPresent(y->y.sendMessage(messageToSend)));
				}
				
				event.getChannel().sendMessage("Aight boss I sent the message --> " + messageToSend);
				return;
			} else if (message.length() > 4 && message.substring(0, 5).equalsIgnoreCase("convo")) { // Send a random DM to a random person (chaotic)
				startConvo();
				return;
			}
				
		} 
		
		// If its not a command, but just basic text
		
		//Initial bot test
		if (message.equalsIgnoreCase("ping")) {
            event.getChannel().sendMessage("Pong!");
        }
		// Check for greetings
		else if ((contains(message, new String[]{"hello", "hi", "yo", "greetings"}) || (contains(message, new String[] {"good"}) && contains(message, new String[] {"morning", "morrow", "afternoon", "everning", "day"}))) && (contains(message, new String[]{"arty", "artagan", "art", "articus", "traveler"}) || message.length() < 19)) {
			event.getChannel().sendMessage(new String[] {"Oh hello there.", "Good day to you.", "Good morning.", "Waheyo", "Greetings.", "Salutations"}[(int)(4*Math.random())]);
		} else if (message.length() > 3) {
			double simScore = -1;
			int index = -1;
			int chatIndex = -1;
			for (int i = 0; i < chats.size(); i++) {
				double[] yeet = chats.get(i).getMostSimilarMessage(message);
				if (yeet[1] > simScore) {
					index = (int)yeet[0];
					simScore = yeet[1];
					chatIndex = i;
				}
			}
			if (chatIndex == -1 || index == -1) { // Placeholder text to send if it can't find anything
				event.getChannel().sendMessage(new String[] {"Hmmm...", "Not sure how to reply to that", "Fascinating", "Intruiging", "lol", "Cool story", "That's wonderful", "Fancy", "Fun times", "Coolbeans", "Pog", "Poggers"}[(int)(12*Math.random())]);
			} else {
				String reply = chats.get(chatIndex).getReply(simScore, index);
				System.out.println("Sending message --> " + reply);
				event.getChannel().sendMessage(reply);
				if (Math.random() > 0.85) {
					newStatus();
					if (Math.random() > 0.7) {
						api.updateActivity(chats.get(chatIndex).getStatus(index));
					}
				}
			}
		}
	}
	
	public static boolean isAllowedChannel(long id) {
		for (long e : allowedChannels) {
			if (e == id)
				return true;
		}
		return false;
	}
	
	public static boolean isOne(String s, String[] list) {
		for (String e : list)
			if (e.equalsIgnoreCase(s))
				return true;
		return false;
	}
	
	public static boolean contains(String s, String[] list) {
		String[] yoink = s.split(" ");
		for (String e : list)
			if (e.indexOf(" ") != -1) {
				if (s.toLowerCase().indexOf(e.toLowerCase()) != -1)
					return true;
			} else {
				for (String f : yoink)
					if (StringSimilarity.similarity(e, f) > 0.85)
						return true;
			}
		return false;
	}
	
}
