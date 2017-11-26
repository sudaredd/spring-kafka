package msg.model;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document
public class TwitterUser {

private String dateTime;
	
	private String userName;
	
	private String tweetMessage;

	public static TwitterUser newInstance(String[] msg) {

		return new TwitterUser(msg[0], msg[1], msg[2]);
	}
	
	
}
