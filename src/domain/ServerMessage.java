package domain;

public class ServerMessage {

	private int mid;
	private int flag;
	private String message;

	public ServerMessage(int mid, int flag, String message) {
		super();
		this.mid = mid;
		this.flag = flag;
		this.message = message;
	}

	public int getMid() {
		return mid;
	}

	public void setMid(int mid) {
		this.mid = mid;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
