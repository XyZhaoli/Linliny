package domain;

public class MessageEvent {

	private byte[] cmd;
	private int tag;

	public MessageEvent(byte[] cmd, int tag) {
		this.cmd = cmd;
		this.tag = tag;
	}

	public byte[] getCmd() {
		return cmd;
	}

	public void setCmd(byte[] cmd) {
		this.cmd = cmd;
	}

	public int getTag() {
		return tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}

}
