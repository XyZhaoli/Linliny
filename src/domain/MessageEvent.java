package domain;

public class MessageEvent {

	//命令执行成功
	public static final int SEND_CMD_SUCCESS = 0;
	
	//出货失败，发送的命令格式错误
	public static final int SEND_CMD_FORMAT_ERROR = 1;
	
	//出货失败机器正忙
	public static final int SEND_CMD_MACHINE_BUSY = 2;
	
	//命令执行超出起范围
	public static final int SEMD_CMD_EXECUTE_ERROR = 3;
	// 消息的标志
	public int flag;
	// 消息的内容
	public String message;

	public MessageEvent(int flag, String message) {
		this.flag = flag;
		this.message = message;
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
