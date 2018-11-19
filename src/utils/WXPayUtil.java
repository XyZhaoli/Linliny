package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WXPayUtil {
	/**
	 * 生成orderid订单号
	 * 
	 * @return
	 */
	public static String getorderid() {
		Date data = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssS");
		String orderid = format.format(data) + (int) ((Math.random() * 9 + 1) * 100000);
		return orderid;
	}
}
