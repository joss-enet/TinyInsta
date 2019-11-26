package almaCorp;

import java.io.Serializable;

import com.google.appengine.api.datastore.Text;

public class PostRequestBody implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private Text image;
	private String message;
	
	public PostRequestBody() { }

	public Text getImage() {
		return image;
	}

	public void setImage(Text image) {
		this.image = image;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	};
	
	

}
