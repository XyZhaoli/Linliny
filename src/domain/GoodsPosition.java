package domain;

public class GoodsPosition {
	// "0B-06" 行号 列号
	// 商品在货架上的行号
	int rowNum;
	// 商品在货架上的列 号
	int columnNum;

	String Yid;

	String Yname;

	boolean isRemoveGoods;

	public GoodsPosition(int rowNum, int columnNum, String yid, String yname, boolean isRemoveGoods) {
		this.rowNum = rowNum;
		this.columnNum = columnNum;
		this.Yid = yid;
		this.Yname = yname;
		this.isRemoveGoods = isRemoveGoods;
	}
	
	public GoodsPosition(int rowNum, int columnNum, String yid, String yname) {
		this.rowNum = rowNum;
		this.columnNum = columnNum;
		this.Yid = yid;
		this.Yname = yname;
	}

	public boolean isRemoveGoods() {
		return isRemoveGoods;
	}
	

	public void setRemoveGoods(boolean isRemoveGoods) {
		this.isRemoveGoods = isRemoveGoods;
	}

	public GoodsPosition(int rowNum, int columnNum) {
		super();
		this.rowNum = rowNum;
		this.columnNum = columnNum;
	}

	public String getYname() {
		return Yname;
	}

	public void setYname(String yname) {
		Yname = yname;
	}

	public String getYid() {
		return Yid;
	}

	public void setYid(String yid) {
		Yid = yid;
	}

	public int getRowNum() {
		return rowNum;
	}

	public void setRowNum(int rowNum) {
		this.rowNum = rowNum;
	}

	public int getColumnNum() {
		return columnNum;
	}

	public void setColumnNum(int columnNum) {
		this.columnNum = columnNum;
	}
}
