package android_serialport_api;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import activity.ShoppingCarActivity;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.sample.R;
import domain.AlreadyToBuyGoods;
import utils.ShoppingCarManager;

public class MyAdapterCart extends BaseAdapter implements View.OnClickListener {
	private Context context;
	private double totalPrice = 0;
	private double totalCount = 0;
	private ShoppingCarActivity iu;
	private List<ViewHolder> viewHolders = new ArrayList<MyAdapterCart.ViewHolder>();

	private List<AlreadyToBuyGoods> buyGoods ;

	public MyAdapterCart(List<AlreadyToBuyGoods> list) {
		super();
		this.buyGoods = list;
	}

	@Override
	public int getCount() {
		return buyGoods.size();
	}

	@Override
	public AlreadyToBuyGoods getItem(int i) {
		return buyGoods.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(final int position, View view, ViewGroup viewGroup) {
		final ViewHolder viewHolder;
		if (context == null)
			context = viewGroup.getContext();
		if (view == null) {
			view = LayoutInflater.from(context).inflate(R.layout.shoppingitem, null);
			viewHolder = new ViewHolder();
			viewHolder.shoppingName = (TextView) view.findViewById(R.id.shoppingName);
			viewHolder.minus = (ImageButton) view.findViewById(R.id.minus);
			viewHolder.delete = (ImageView) view.findViewById(R.id.delete);
			viewHolder.plus = (ImageButton) view.findViewById(R.id.plus);
			viewHolder.shoppingPrice = (TextView) view.findViewById(R.id.shoppingPrice);
			viewHolder.shoppingNumber = (TextView) view.findViewById(R.id.shoppingNumber);
			viewHolder.shoppingtotal = (TextView) view.findViewById(R.id.shoppingtotal);

			iu = (ShoppingCarActivity) context;
			viewHolder.shoppingtotalnum = (TextView) iu.findViewById(R.id.tv_goods_num_shopping_car);
			viewHolder.shoppingtotalzon = (TextView) iu.findViewById(R.id.tv_goods_price_shopping_car);

			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		viewHolder.shoppingName.setText(buyGoods.get(position).getAleardyBuyGoods().getYname()); // 商品的名字
		viewHolder.shoppingPrice.setText(buyGoods.get(position).getAleardyBuyGoods().getPrice());// 商品的单价

		setViewHolder(viewHolder, position);

		// 单个商品的数量
		int signalGoodsNum = buyGoods.get(position).getAlreadyToBuyGoodsnum();
		int inventory = Integer.parseInt(buyGoods.get(position).getAleardyBuyGoods().getZongshu()); // 商品的库存

		if (signalGoodsNum > inventory)
			signalGoodsNum = inventory;
		try {
			buyGoods.get(position).setAlreadyToBuyGoodsnum(signalGoodsNum);
		} catch (Exception e3) {
			e3.printStackTrace();
		}
		viewHolder.shoppingNumber.setText(String.valueOf(signalGoodsNum));

		// 单个商品的单价
		String signalGoodsPriceStr = buyGoods.get(position).getAleardyBuyGoods().getPrice();

		double signalGoodsTotalPrice = signalGoodsNum * Double.parseDouble(signalGoodsPriceStr);
		buyGoods.get(position).setAlreadyToBuyGoodsPrice(signalGoodsTotalPrice);
		// 总价格的显示
		DecimalFormat df = new DecimalFormat("0.00");
		String CNY = "￥" + df.format(signalGoodsTotalPrice);
		viewHolder.shoppingtotal.setText(CNY);

		for (int e = 0; e < buyGoods.size(); e++) {
			try {
				// 将单个商品的总价相加 计算出所有商品的总价
				totalPrice = totalPrice + buyGoods.get(e).getAlreadyToBuyGoodsPrice();
			} catch (Exception e2) {
				totalPrice += totalPrice;
			}
			// 计算出所有订单商品的数量s
			totalCount = totalCount + buyGoods.get(e).getAlreadyToBuyGoodsnum();
		}

		String totalPriceStr = "￥" + df.format(totalPrice);
		viewHolder.shoppingtotalzon.setText(totalPriceStr);
		viewHolder.shoppingtotalnum.setText(
				Html.fromHtml("共" + "<font color='#FF0000'>" + String.valueOf((int) totalCount) + "</font>" + "件商品"));

		// 设置监听事件
		viewHolder.minus.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				int goodsNum = buyGoods.get(position).getAlreadyToBuyGoodsnum();
				if (goodsNum > 1) {
					goodsNum--;
					try {
						buyGoods.get(position).setAlreadyToBuyGoodsnum(goodsNum);
					} catch (Exception e) {
						e.printStackTrace();
						try {
							buyGoods.get(position).setAlreadyToBuyGoodsnum(0);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
					notifyDataSetChanged();// 刷新
					sendBrodcast();
				} else {
					Toast.makeText(context, "不能再减少啦", Toast.LENGTH_SHORT).show();
				}
			}
		});
		viewHolder.plus.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				int goodsNum = buyGoods.get(position).getAlreadyToBuyGoodsnum();
				int goodsInvertory = Integer.parseInt(buyGoods.get(position).getAleardyBuyGoods().getZongshu());
				if (goodsNum < goodsInvertory) {
					goodsNum++;
					try {
						buyGoods.get(position).setAlreadyToBuyGoodsnum(goodsNum);
					} catch (Exception e) {
						e.printStackTrace();
					}
					notifyDataSetChanged();
					sendBrodcast();
				} else {
					Toast.makeText(context, "已达到商品可购买最大数量了哦", Toast.LENGTH_SHORT).show();
				}
			}
		});
		viewHolder.delete.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				buyGoods.remove(position);
				notifyDataSetChanged();
				if (!refreshPriceAndCount(getViewHolder(position))) {
					Toast.makeText(context, "数据刷新出错", Toast.LENGTH_SHORT).show();
				}
				removeViewHolder(position);
			}
		});
		totalPrice = 0;
		totalCount = 0;
		return view;
	}

	private void setViewHolder(ViewHolder viewHolder, int position) {
		if (viewHolder != null) {
			viewHolders.add(position, viewHolder);
		}
	}

	private void removeViewHolder(int position) {
		viewHolders.remove(position);
	}

	private ViewHolder getViewHolder(int position) {
		return position <= getCount() ? viewHolders.get(position) : null;
	}

	@Override
	public void onClick(View view) {
	}

	static class ViewHolder {
		TextView shoppingName;
		ImageButton minus;
		ImageView delete;
		ImageButton plus;
		TextView shoppingPrice;
		TextView shoppingNumber;
		TextView shoppingtotal;
		TextView shoppingtotalnum;
		TextView shoppingtotalzon;
	}

	private boolean refreshPriceAndCount(ViewHolder viewHolder) {
		if (viewHolder != null) {
			DecimalFormat df = new DecimalFormat("0.00");
			for (int i = 0; i < buyGoods.size() - 1; i++) {
				// 订单总价
				totalPrice = totalPrice + buyGoods.get(i).getAlreadyToBuyGoodsPrice();
				// 订单总数
				totalCount = totalCount + buyGoods.get(i).getAlreadyToBuyGoodsnum();
			}
			String ZON = "￥" + df.format(totalPrice);
			viewHolder.shoppingtotalzon.setText(ZON);
			viewHolder.shoppingtotalnum.setText(Html
					.fromHtml("共" + "<font color='#FF0000'>" + String.valueOf((int) totalCount) + "</font>" + "件商品"));
			sendBrodcast();
			return true;
		}
		return false;
	}

	private void sendBrodcast() {
		Intent intent = new Intent();
		intent.setAction("getGoodsNumAction");
		context.sendBroadcast(intent);
	}

}