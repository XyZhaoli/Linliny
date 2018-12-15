package domain;

public class MessageEvent {

	public Object obj;
	public int flag;

	public MessageEvent(Object obj, int flag) {
		super();
		this.obj = obj;
		this.flag = flag;
	}

	public MessageEvent(int flag) {
		super();
		this.flag = flag;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

}
