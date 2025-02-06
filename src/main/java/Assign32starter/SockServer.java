package Assign32starter;
import java.net.*;
import java.util.Base64;
import java.util.Set;
import java.util.Stack;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import java.awt.image.BufferedImage;
import java.io.*;
import org.json.*;


/**
 * A class to demonstrate a simple client-server connection using sockets.
 * Ser321 Foundations of Distributed Software Systems
 */
public class SockServer {
	static final Map<String, String[]> locationImages = new HashMap<>();
	static {
		locationImages.put("Grand Canyon", new String[]{"img/GrandCanyon1.png", "img/GrandCanyon2.png", "img/GrandCanyon3.png", "img/GrandCanyon4.png"});
		locationImages.put("Stonehenge", new String[]{"img/Stonehenge1.png", "img/Stonehenge2.png", "img/Stonehenge3.png", "img/Stonehenge4.png"});
		locationImages.put("Colosseum", new String[]{"img/Colosseum1.png", "img/Colosseum2.png", "img/Colosseum3.png", "img/Colosseum4.png"});
	}

	public static void main(String args[]) {
		Socket sock;
		try {
			ServerSocket serv = new ServerSocket(8888);
			System.out.println("Server ready for connection");

			String name = "";
			int points = 0;
			String currentLocation = "Grand Canyon";
			int imageIndex = 0;
			boolean inGame = false; // Track if the user is playing the game

			while (true) {
				sock = serv.accept();
				ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
				PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);

				String s = (String) in.readObject();
				JSONObject json = new JSONObject(s);
				JSONObject response = new JSONObject();

				if (!inGame) {
					if (json.getString("type").equals("start")) {
						// Step 1: Initial Start
						System.out.println("- Got a start");
						response.put("type", "hello");
						response.put("value", "Hello, please tell me your name.");
						sendImg("img/hi.png", response); // Send initial image
					} else if (json.getString("type").equals("name")) {
						// Step 2: User enters their name
						name = json.getString("value");
						System.out.println("User name: " + name);
						response.put("type", "prompt");
						response.put("value", "Hello " + name + "!\nWhat would you like to do: start, leaderboard, or quit?");
					} else if (json.getString("type").equals("choice")) {
						// Step 3: Handle user's choice
						String choice = json.getString("value");

						if (choice.equalsIgnoreCase("start")) {
							inGame = true; // Enter game mode
							points = 0; // Reset points
							imageIndex = 0; // Reset to the first image
							response.put("type", "game");
							response.put("value", "Game started! Points: 0");
							response.put("points", points);
							sendImg(locationImages.get(currentLocation)[imageIndex], response);
						} else if (choice.equalsIgnoreCase("leaderboard")) {
							response.put("type", "leaderboard");
							response.put("value", "Leaderboard functionality not implemented yet.");
						} else if (choice.equalsIgnoreCase("quit")) {
							response.put("type", "quit");
							response.put("value", "Goodbye! Thanks for playing.");
						} else {
							response.put("type", "error");
							response.put("value", "Unknown choice. Please select start, leaderboard, or quit.");
						}
					}
				} else {
					// Game logic: Process guesses
					String guess = json.getString("value");

					if (guess.equalsIgnoreCase(currentLocation)) {
						points += 10;
						response.put("type", "correct");
						response.put("value", "Correct! You've earned 10 points. Total points: " + points);
						response.put("points", points);

						// Cycle to the next location
						currentLocation = getNextLocation(currentLocation);
						imageIndex = 0;
						sendImg(locationImages.get(currentLocation)[imageIndex], response);
					} else {
						imageIndex++;

						if (imageIndex < locationImages.get(currentLocation).length) {
							response.put("type", "hint");
							response.put("value", "Incorrect! Here's another hint.");
							sendImg(locationImages.get(currentLocation)[imageIndex], response);
						} else {
							response.put("type", "gameover");
							response.put("value", "Game over! The correct answer was: " + currentLocation + ". Total points: " + points);
							inGame = false; // Exit game mode
						}
					}
				}

				outWrite.println(response.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getNextLocation(String currentLocation) {
		List<String> locations = new ArrayList<>(locationImages.keySet());
		int index = locations.indexOf(currentLocation);
		return locations.get((index + 1) % locations.size());
	}

	public static JSONObject sendImg(String filename, JSONObject obj) throws Exception {
		File file = new File("C:\\Users\\brand\\SER321\\Assignment3\\Assign3-2\\Starter3-2\\img\\" + filename);
		if (file.exists()) {
			BufferedImage image = ImageIO.read(file);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			String encodedImage = Base64.getEncoder().encodeToString(baos.toByteArray());
			obj.put("image", encodedImage);
		} else {
			obj.put("image", "Image not found: " + filename);
			obj.put("error", "Image file not found: " + filename);
		}
		return obj;
	}
}

