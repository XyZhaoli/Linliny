package domain;

public class MachineState {

	// 机器状态码
	int machineStateCode;
	// 机器故障码
	int machineMalfunctionCode;
	// 温度值
	float temper;
	// 湿度值
	float humidity;
	// 当前位置
	GoodsPosition goodsPosition;

	public MachineState(int machineStateCode, int machineMalfunctionCode, float temper, float humidity,
			GoodsPosition goodsPosition) {
		this.machineStateCode = machineStateCode;
		this.machineMalfunctionCode = machineMalfunctionCode;
		this.temper = temper;
		this.humidity = humidity;
		this.goodsPosition = goodsPosition;
	};

	public int getMachineStateCode() {
		return machineStateCode;
	}

	public void setMachineStateCode(int machineStateCode) {
		this.machineStateCode = machineStateCode;
	}

	public int getMachineMalfunctionCode() {
		return machineMalfunctionCode;
	}

	public void setMachineMalfunctionCode(int machineMalfunctionCode) {
		this.machineMalfunctionCode = machineMalfunctionCode;
	}

	public float getTemper() {
		return temper;
	}

	public void setTemper(float temper) {
		this.temper = temper;
	}

	public float getHumidity() {
		return humidity;
	}

	public void setHumidity(float humidity) {
		this.humidity = humidity;
	}

	public GoodsPosition getGoodsPosition() {
		return goodsPosition;
	}

	public void setGoodsPosition(GoodsPosition goodsPosition) {
		this.goodsPosition = goodsPosition;
	}

}
