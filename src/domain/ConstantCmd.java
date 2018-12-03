package domain;

public class ConstantCmd {

	//获取机器的状态
	public static final int get_machine_state_cmd = 0x10;

	//还篮子的指令
	public static final int get_return_basket_cmd = 0x70;

	//请求出货的指令
	public static final int get_request_shipment_cmd = 0x20;
	
	public static final byte[] getMachineStateCmd = new byte[] { 0x02, 0x03, 0x10, 0x15 };
	
	public static final String BASE_URLS = "http://linliny.com/dingyifeng_web/";
	
	public static final String GET_APP_VERSION = "http://linliny.com/linlinyapp/linlinyapp.json";
}
