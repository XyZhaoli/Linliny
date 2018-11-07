package utils;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import domain.AlreadyToBuyGoods;
import domain.Goods;
import domain.GoodsPosition;

public class ShoppingCarManager {

	// 这是添加到购物车汇中的商品的列表
	private static List<AlreadyToBuyGoods> shoppingCarGoods = new ArrayList<AlreadyToBuyGoods>();
	// 这是从服务器获取的商品列表信息
	private static List<Goods> fromNetWorkGoodsList = new ArrayList<Goods>();
	private static ShoppingCarManager shoppingCarManager;
	private static List<GoodsPosition> goodsPositions = new ArrayList<GoodsPosition>();;

	public static ShoppingCarManager getInstence() {
		if (shoppingCarManager == null) {
			synchronized (ShoppingCarManager.class) {
				if (shoppingCarManager == null) {
					shoppingCarManager = new ShoppingCarManager();
				}
			}
		}
		return shoppingCarManager;
	}

	public List<AlreadyToBuyGoods> getShoppingCarGoods() {
		return shoppingCarGoods;
	}

	public void setShoppingCarGoods(List<AlreadyToBuyGoods> shoppingCarGoods) {
		ShoppingCarManager.shoppingCarGoods = shoppingCarGoods;
	}

	public void addGoodsToCar(AlreadyToBuyGoods goods) throws Exception {
		int flag = 0;
		for (int i = 0; i < shoppingCarGoods.size(); i++) {
			AlreadyToBuyGoods alreadyToBuyGoods = shoppingCarGoods.get(i);
			if (alreadyToBuyGoods.getAleardyBuyGoods().getYid().equals(goods.getAleardyBuyGoods().getYid())) {
				throw new Exception("列表中已经有了这个商品，请不要重复添加");
			} else {
				flag++;
			}
		}
		if (flag == shoppingCarGoods.size()) {
			shoppingCarGoods.add(goods);
		}
		if (shoppingCarGoods.size() == 0) {
			shoppingCarGoods.add(goods);
		}
	}

	public int getCarGoodsAllCount() {
		// 所有商品的数量
		int goodsQuantity = 0;
		int num;
		for (int i = 0; i < shoppingCarGoods.size(); i++) {
			AlreadyToBuyGoods alreadyToBuyGoods = shoppingCarGoods.get(i);
			try {
				num = alreadyToBuyGoods.getAlreadyToBuyGoodsnum();
			} catch (Exception e) {
				num = 0;
			}
			goodsQuantity += num;
		}
		return goodsQuantity;
	}

	public Goods getGoods(String Yid) {
		for (Goods goods : fromNetWorkGoodsList) {
			if (goods.getYid().equals(Yid)) {
				return goods;
			}
		}
		return null;
	}

	public List<Goods> getFromNetWorkGoods() {
		return fromNetWorkGoodsList;
	}

	public void setFromNetWorkGoods(List<Goods> fromNetWorkGoods) {
		ShoppingCarManager.fromNetWorkGoodsList.clear();
		ShoppingCarManager.fromNetWorkGoodsList.addAll(fromNetWorkGoods);
	}

	/**
	 * 获取已购商品的在货架上的位置
	 * 
	 * @return
	 */
	public List<GoodsPosition> getGoodsPositions() {
		goodsPositions.clear();
		for (AlreadyToBuyGoods alreadyToBuyGoods : shoppingCarGoods) {
			String untiekes = alreadyToBuyGoods.getAleardyBuyGoods().getUntiekes();
			String Yid = alreadyToBuyGoods.getAleardyBuyGoods().getYid();
			String goodsName = "";
			try {
				goodsName = paseYname(alreadyToBuyGoods.getAleardyBuyGoods().getYname());
			} catch (Exception e) {
				goodsName = alreadyToBuyGoods.getAleardyBuyGoods().getYname();
			}
			int alreadyToBuyGoodsnum = alreadyToBuyGoods.getAlreadyToBuyGoodsnum();
			try {
				String[] positionStrArray = untiekes.split(",");
				for (int i = 0; i < alreadyToBuyGoodsnum; i++) {
					String[] rowAndColumnStr = positionStrArray[i].split("-");
					Log.e("columnNum:rowNum", rowAndColumnStr[0] + ":" + rowAndColumnStr[1]);
					GoodsPosition goodsPosition = new GoodsPosition(Integer.parseInt(rowAndColumnStr[0], 16),
							Integer.parseInt(rowAndColumnStr[1]), Yid, goodsName);
					goodsPositions.add(goodsPosition);
				}
			} catch (Exception e) {
			}
		}
		return goodsPositions;
	}

	private String paseYname(String yname) {
		int start = yname.indexOf("【") + 1;
		int end = yname.indexOf("】");
		String name = yname.substring(start, end);
		return name;
	}

	public void clearShoppingCar() {
		shoppingCarGoods.clear();
	}

}
