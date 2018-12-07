package utils;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.annotation.SuppressLint;
import android.util.Log;
import domain.ConstantCmd;
import domain.GoodsPosition;
import domain.MachineState;
import uartJni.Uartjni;

public class MachineStateManager {

	private static MachineStateManager stateManager;
	private static MachineState machineState;
	private static Uartjni uartjni;
	private static String mid;
	private static final byte[] cmd = new byte[] { 0x02, 0x03, 0x10, 0x15 };

	private static void parseMachineState(byte[] obj) {
		// 防止数组出现越界的异常
		try {
			// 机器状态码
			int machineStateCode = obj[3];
			// 机器故障码
			int machineMalfunctionCode = obj[4];
			// 温度值
			int temH = obj[5] & 0xff;
			int temL = obj[6] & 0xff;
			float temper = ((float) ((temH << 8) | temL) / 10);
			// 湿度值
			int humidityH = obj[7] & 0xff;
			int humidityL = obj[8] & 0xff;
			float humidity = ((float) ((humidityH << 8) | humidityL) / 10);
			// 当前位置
			GoodsPosition goodsPosition = new GoodsPosition(obj[9], obj[10]);
			Log.e("parseMachineState",
					machineStateCode + " : " + machineMalfunctionCode + " : " + temper + " : " + humidity);
			machineState = new MachineState(machineStateCode, machineMalfunctionCode, temper, humidity, goodsPosition);
		} catch (Exception e) {
			Log.e("parseMachineState", byteToHexstring(obj, obj.length));
		}
	}

	private MachineStateManager() {
		initSerial();
	}

	private void initSerial() {
		uartjni = new Uartjni() {
			public void onNativeCallback(byte[] arg1) {
				parseMachineState(arg1);
			};
		};
		uartjni.nativeInitilize();
		uartjni.BoardThreadStart();
	}

	public void closeSerial() {
		if (uartjni != null) {
			ThreadManager.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					uartjni.NativeThreadStop();
					uartjni = null;
					stateManager = null;
				}
			});
		}
	}

	public static MachineStateManager getInstance() {
		if (stateManager == null) {
			synchronized (MachineStateManager.class) {
				if (stateManager == null) {
					stateManager = new MachineStateManager();
				}
			}
		}
		sendCmd();
		return stateManager;
	}

	public static void sendCmd() {
		if(uartjni != null) {
			uartjni.UartWriteCmd(ConstantCmd.getMachineStateCmd, ConstantCmd.getMachineStateCmd.length);
		}
	}

	public MachineState getMachineState() {
		if (machineState == null) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			sendCmd();
		}
		return machineState;
	}

	public void reportMachineState() {
		if (getMachineState().getMachineStateCode() == 0x09) {
			final String parseMachineFaultCode = parseMachineFaultCode(machineState.getMachineMalfunctionCode());
			// TODO 此时机器处于故障状态，上报给服务器此时的状态
			ThreadManager.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					HttpUtils httpUtils = new HttpUtils();
					StringBuffer url = new StringBuffer("http://linliny.com/dingyifeng_web/AddFailure.json");
					url.append("?Fcode=").append(machineState.getMachineStateCode()).append("&Fname=")
							.append(machineState.getMachineMalfunctionCode()).append("&Fcontent=")
							.append(parseMachineFaultCode).append("&Fresolve=暂无&Funwound=0&Mid=").append(Util.getMid());
					httpUtils.send(HttpMethod.POST, url.toString(), new RequestCallBack<String>() {

						@Override
						public void onFailure(HttpException arg0, String arg1) {
							
						}

						@Override
						public void onSuccess(ResponseInfo<String> arg0) {
							
						}
					});
				}
			});
		}
	}

	private String parseMachineFaultCode(int machineMalfunctionCode) {
		/**
		 * 0X01:定位故障(主电机或位置光电开关故障)； 0X02:开门故障； 0X03:关门故障； 0X04:防夹传感器故障；
		 * 0X05:制冷故障； 0X06:加湿故障（缺水或长期湿度达不到设定值）； 0X07:其他故障； 0X08:主电机故障。
		 */
		String faultCodeStr = "";
		switch (machineMalfunctionCode) {
		case 0X01:
			faultCodeStr = "定位故障(主电机或位置光电开关故障)";
			break;
		case 0X02:
			faultCodeStr = "开门故障";
			break;
		case 0X03:
			faultCodeStr = "关门故障";
			break;
		case 0X04:
			faultCodeStr = "防夹传感器故障";
			break;
		case 0X05:
			faultCodeStr = "制冷故障";
			break;
		case 0X06:
			faultCodeStr = "加湿故障(缺水或长期湿度达不到设定值)";
			break;
		case 0X07:
			faultCodeStr = "其他故障";
			break;
		case 0X08:
			faultCodeStr = "主电机故障";
			break;
		default:
			faultCodeStr = "未知故障";
			break;
		}
		return faultCodeStr;
	}

	/**
	 * 函数说明：将byte数组转化位16进制的字符串
	 * 
	 * @param length
	 *            所要转化数组的长度
	 * @param buff
	 *            byte数组
	 * @return 返回转化完成以后的十六进制字符串
	 */
	@SuppressLint("DefaultLocale")
	public static String byteToHexstring(byte[] buff, int length) {
		String HexString = "";
		for (int i = 0; i < buff.length; i++) {
			String hex = Integer.toHexString(buff[i] & 0xff);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			HexString += hex.toUpperCase() + " ";
		}
		return HexString;
	}

}
