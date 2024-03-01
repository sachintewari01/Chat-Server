import java.io.*;

public class ChatMessage implements Serializable {
	static final int JOINED = 0, MESSAGE = 1, LOGOUT = 2, TIME=3, NOOFUSERS=4, GOOGLE=5,YOUTUBE=6;
	private int type;
	private String message;

	ChatMessage(int type, String message) {
		this.type = type;
		this.message = message;
	}
	
	int getType() {
		return type;
	}

	String getMessage() {
		return message;
	}
}
