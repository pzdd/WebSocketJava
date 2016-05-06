package br.com.socket;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@ServerEndpoint("/chatServer")
public class ServerEndPoint {
	
	static Set<Session> chatUsers = Collections.synchronizedSet(new HashSet<Session>());
	static JSONArray array = new JSONArray();
	
	@OnOpen
	public void handleOpen(Session userSession){
		chatUsers.add(userSession);
	}
	
	@OnMessage
	public  void handleMessage(String message,Session userSession) throws IOException{
		String user = (String) userSession.getUserProperties().get("username");
		try(JsonReader reader = Json.createReader(new StringReader(message))){
			JsonObject jsonMessage = reader.readObject();
			if(user == null){
				user = "usuario"+userSession.getId();
				userSession.getUserProperties().put("username", user);
				
			}
			Iterator<Session> iterator = chatUsers.iterator();
			while(iterator.hasNext()){
				iterator.next().getBasicRemote().sendText(buildJsonData(user,jsonMessage.getJsonNumber("lat"),jsonMessage.getJsonNumber("lng")));
			}
		}
	}
	
	@OnClose
	public void handleClose(Session userSession){
		chatUsers.remove(userSession);
	}
	
	@SuppressWarnings("unchecked")
	public String buildJsonData(String username, JsonNumber lat,JsonNumber lng) throws IOException{
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("username", username);
		jsonObj.put("lat", lat);
		jsonObj.put("lng", lng);
		
		array.add(jsonObj);
		
		StringWriter writer = new StringWriter();
		array.writeJSONString(writer);
		
		return writer.toString();
	}

}
