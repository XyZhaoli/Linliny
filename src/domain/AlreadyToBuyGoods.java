package domain;

public class AlreadyToBuyGoods {

	private Goods AleardyBuyGoods;

	// 购买商品的数量
	private int alreadyToBuyGoodsNum;
	private double alreadyToBuyGoodsPrice;

	public AlreadyToBuyGoods(Goods aleardyBuyGoods, int alreadyToBuyGoodsnum) {
		this.AleardyBuyGoods = aleardyBuyGoods;
		this.alreadyToBuyGoodsNum = alreadyToBuyGoodsnum;
	}

	public double getAlreadyToBuyGoodsPrice() {
		return alreadyToBuyGoodsNum * Double.parseDouble(AleardyBuyGoods.getPrice());
	}

	public void setAlreadyToBuyGoodsPrice(double alreadyToBuyGoodsPrice) {
		this.alreadyToBuyGoodsPrice = alreadyToBuyGoodsPrice;
	}

	public Goods getAleardyBuyGoods() {
		return AleardyBuyGoods;
	}

	public void setAleardyBuyGoods(Goods aleardyBuyGoods) {
		AleardyBuyGoods = aleardyBuyGoods;
	}

	public int getAlreadyToBuyGoodsnum() {
		return alreadyToBuyGoodsNum;
	}

	public void setAlreadyToBuyGoodsnum(int alreadyToBuyGoodsnum) throws Exception {
		if (alreadyToBuyGoodsnum >= 0) {
			this.alreadyToBuyGoodsNum = alreadyToBuyGoodsnum;
		} else {
			throw new Exception("商品数量不能为负数");
		}
	}

}
