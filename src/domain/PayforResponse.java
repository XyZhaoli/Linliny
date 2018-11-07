package domain;

public class PayforResponse {
	String wechatRes;
	String aliPayRes;

	public PayforResponse(String wechatRes, String aliPayRes) {
		super();
		this.wechatRes = wechatRes;
		this.aliPayRes = aliPayRes;
	}

	public String getWechatRes() {
		return wechatRes;
	}

	public void setWechatRes(String wechatRes) {
		this.wechatRes = wechatRes;
	}

	public String getAliPayRes() {
		return aliPayRes;
	}

	public void setAliPayRes(String aliPayRes) {
		this.aliPayRes = aliPayRes;
	}

}
