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
	
	//APP 目前的模式  0表示机器当前的状态是正常的
	public static int currentStatus = 0;

	
	public static final int MACHINE_NORMAL = 0;
	
	//服务器进入升级模式
	public static final int MACHINE_UPDATE = 1;
	
	//机器进入维护模式
	public static final int MACHINE_MAINTENANCE = 2;
	
}
