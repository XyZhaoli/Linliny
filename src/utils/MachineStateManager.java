package utils;

import java.util.HashMap;
import java.util.Map;

import com.iflytek.cloud.thirdparty.v;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.RequestParams;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import domain.GoodsPosition;
import domain.MachineState;
import uartJni.Uartjni;

public class MachineStateManager {

	private static MachineStateManager stateManager;
	private static MachineState machineState;
	private static Uartjni uartjni;
	private static String mid;

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
		if(uartjni != null) {
			uartjni.NativeThreadStop();
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

	public MachineState getMachineState() {
		if (machineState == null) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return machineState;
	}

	public void reportMachineState() {
		/**
		 * failureRecord.setFcode(map.get("Fcode").toString()); //故障代码
		 * failureRecord.setFname(map.get("Fname").toString()); //故障名称
		 * failureRecord.setFcontent(map.get("Fcontent").toString()); //故障内容
		 * failureRecord.setFresolve(map.get("Fresolve").toString()); //解决方案
		 * failureRecord.setFunwound((int)map.get("Funwound")); //是否解除
		 * failureRecord.setMid((int)map.get("Mid")); //机器id·
		 */
		if (getMachineState().getMachineStateCode() == 0x09) {
			String parseMachineFaultCode = parseMachineFaultCode(machineState.getMachineMalfunctionCode());
			// TODO 此时机器处于故障状态，上报给服务器此时的状态
//			final Map<String, Object> map = new HashMap<String, Object>();
//			map.put("Fcode", machineState.getMachineMalfunctionCode() + "");
//			map.put("Fname", parseMachineFaultCode);
//			map.put("Fcontent", "参考故障名称");
//			map.put("Fresolve", "暂无");
//			map.put("Funwound", "否");
//			map.put("Mid", utils.Util.getMid());
//			new Thread() {
//				public void run() {
//					bb.doPost("http://linliny.com/dingyifeng_web/AddFailure.json", map);
//				};
//			}.start();
		}
	}

	private String parseMachineFaultCode(int machineMalfunctionCode) {
		/**
		 * 0X01:定位故障(主电机或位置光电开关故障)； 0X02:开门故障； 0X03:关门故障； 0X04:防夹传感器故障； 0X05:制冷故障；
		 * 0X06:加湿故障（缺水或长期湿度达不到设定值）； 0X07:其他故障； 0X08:主电机故障。
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
			break;
		}
		return faultCodeStr;
	}

	private static void sendCmd() {
		byte[] cmd = new byte[] { 0x02, 0x03, 0x10, 0x15 };
		uartjni.UartWriteCmd(cmd, 4);
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
