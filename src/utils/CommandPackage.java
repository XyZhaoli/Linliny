package utils;

import domain.ConstantCmd;

public class CommandPackage {

	public static byte[] getRequestShipment(int CMD, int rowNum, int columnNum) {
		byte[] command = new byte[6];
		int FCC = 0;// 校验和
		command[0] = 0x02;
		command[1] = 0x05; // 命令的长度

		switch (CMD) {
		case ConstantCmd.get_request_shipment_cmd:
			command[2] = ConstantCmd.get_request_shipment_cmd;
			break;
		case ConstantCmd.get_return_basket_cmd:
			command[2] = ConstantCmd.get_return_basket_cmd;
			break;
		default:
			break;
		}
		command[3] = (byte) rowNum;
		command[4] = (byte) columnNum;
		for (int i = 0; i < command[1]; i++) {
			FCC += command[i];
		}
		command[5] = (byte) FCC; // 命令的校验和
		return command;
	}

}
