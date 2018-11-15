package domain;

public class Goods implements Cloneable {
	// 商品的规格
	private String Company;

	// 商品的条码
	private String BarCode;

	// 商品的名称
	private String Title;

	private String Odunbai;

	// 商品条码
	private String zongshu;

	// 商品分类(单品, 套餐)
	private String YTname;

	// 商品ID
	private String Yid;

	// 商品的单价
	private String Price;

	// 商品详情图URL
	private String Picture;

	private String Untiekes;

	// 商品的详情介绍
	private String Yname;
	private String Did;
	private String YTid;

	public Goods(String company, String barCode, String title, String odunbai, String zongshu, String yTname,
			String yid, String price, String picture, String untiekes, String yname, String did, String yTid) {

		this.Company = company;
		this.BarCode = barCode;
		this.Title = title;
		this.Odunbai = odunbai;
		this.zongshu = zongshu;
		this.YTname = yTname;
		this.Yid = yid;
		this.Price = price;
		this.Picture = picture;
		this.Untiekes = untiekes;
		this.Yname = yname;
		this.Did = did;
		this.YTid = yTid;
	}

	public String getCompany() {
		return Company;
	}

	public void setCompany(String company) {
		Company = company;
	}

	public String getBarCode() {
		return BarCode;
	}

	public void setBarCode(String barCode) {
		BarCode = barCode;
	}

	public String getTitle() {
		return Title;
	}

	public void setTitle(String title) {
		Title = title;
	}

	public String getOdunbai() {
		return Odunbai;
	}

	public void setOdunbai(String odunbai) {
		Odunbai = odunbai;
	}

	public String getZongshu() {
		return zongshu;
	}

	public void setZongshu(String zongshu) {
		this.zongshu = zongshu;
	}

	public String getYTname() {
		return YTname;
	}

	public void setYTname(String yTname) {
		YTname = yTname;
	}

	public String getYid() {
		return Yid;
	}

	public void setYid(String yid) {
		Yid = yid;
	}

	public String getPrice() {
		return Price;
	}

	public void setPrice(String price) {
		Price = price;
	}

	public String getPicture() {
		return Picture;
	}

	public void setPicture(String picture) {
		Picture = picture;
	}

	public String getUntiekes() {
		return Untiekes;
	}

	public void setUntiekes(String untiekes) {
		Untiekes = untiekes;
	}

	public String getYname() {
		return Yname;
	}

	public void setYname(String yname) {
		Yname = yname;
	}

	public String getDid() {
		return Did;
	}

	public void setDid(String did) {
		Did = did;
	}

	public String getYTid() {
		return YTid;
	}

	public void setYTid(String yTid) {
		YTid = yTid;
	}

	@Override
	public Goods clone() throws CloneNotSupportedException {
		return (Goods) super.clone();
	}

	@Override
	public String toString() {
		return "Goods [Company=" + Company + ", BarCode=" + BarCode + ", Title=" + Title + ", Odunbai=" + Odunbai
				+ ", zongshu=" + zongshu + ", YTname=" + YTname + ", Yid=" + Yid + ", Price=" + Price + ", Picture="
				+ Picture + ", Untiekes=" + Untiekes + ", Yname=" + Yname + ", Did=" + Did + ", YTid=" + YTid + "]";
	}

}
