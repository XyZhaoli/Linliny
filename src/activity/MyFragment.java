package activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bumptech.glide.Glide;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.Toast;
import android_serialport_api.PersionInfo;
import android_serialport_api.aa;
import android_serialport_api.sample.R;
import dialog.GoodsDetailDialog;
import domain.AlreadyToBuyGoods;
import domain.ConstantCmd;
import domain.GetGoodsJsonInfo;
import domain.Goods;
import utils.GsonUtils;
import utils.ShoppingCarManager;
import utils.ThreadManager;
import utils.Util;
import utils.VoiceUtils;

@SuppressLint({ "NewApi", "HandlerLeak", "InflateParams" })
public class MyFragment extends Fragment implements OnItemClickListener {

	protected static final int SET_GRIDVIEW = 1;
	private GridView gview;
	private List<Map<String, Object>> data_list;
	private SimpleAdapter sim_adapter;
	private int[] endLocations;

	public MyHandler handler = new MyHandler();
	private ShoppingCarManager shoppingCarManager;
	private List<Goods> goodsList;
	private GoodsDetailsActivity activity;
	private FrameLayout layout;
	private String mid;
	private GoodsDetailDialog goodsDetailDialog;

	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				parseMessage((String) msg.obj);
				break;
			case SET_GRIDVIEW:
				setGridView();
				break;
			default:
				break;
			}
		}
	}

	public void parseMessage(final String str) {
		if (!TextUtils.isEmpty(str)) {
			ThreadManager.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					GetGoodsJsonInfo parseJsonWithGson = GsonUtils.parseJsonWithGson(str, GetGoodsJsonInfo.class);
					goodsList = parseJsonWithGson.getGoodsList();
					shoppingCarManager.setFromNetWorkGoods(goodsList);
					List<Map<String, Object>> list = aa.listKeyMaps(str);
					// 获取分类文字
					PersionInfo info = (PersionInfo) getArguments().getSerializable("info");
					String data = info.getNameString();
					// 列表分类显示
					for (Map<String, Object> map : list) {
						map.put("Pic", (String) map.get("Picture"));
						if (map.get("YTname").equals(data)) {
							data_list.add(map);
						} else if (data == null || data.equals("全部商品")) {
							data_list = list;
						}
					}
					Util.sendMessage(handler, SET_GRIDVIEW);
				}
			});
		}
	}

	public void setGridView() {
		// 新建适配器
		String[] from = { "Pic", "Yname", "Price" };
		int[] to = { R.id.image, R.id.text, R.id.Gprice };
		if (data_list != null) {
			sim_adapter = new MySimpleAdapter(data_list, R.layout.item, from, to);
			sim_adapter.setViewBinder(new ViewBinder() {
				public boolean setViewValue(View view, Object data, String textRepresentation) {
					// 判断是否为我们要处理的对象
					if (view instanceof ImageView && !TextUtils.isEmpty(data.toString())) {
						ImageView iv = (ImageView) view;
						// 如果这个图片的URL包含+-+，那么就说明这个商品的图像有多个图片
						String string;
						try {
							string = Util.parseImageUrl(data.toString())[0];
						} catch (Exception e) {
							string = data.toString();
						}
						if (string != null) {
							Glide.with(getActivity()).load(string).into(iv);
						}
						return true;
					} else {
						return false;
					}
				}
			});
			// 配置适配器
			gview.setAdapter(sim_adapter);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getActivity().setFinishOnTouchOutside(true);
		activity = (GoodsDetailsActivity) getActivity();
		endLocations = activity.getLocations();
		// 生成布局文件时，将你的GridView膨胀出来
		View view = inflater.inflate(R.layout.myfragment, null);
		gview = (GridView) view.findViewById(R.id.gview);
		layout = (FrameLayout) view.findViewById(R.id.linear_layout);
		gview.setOnItemClickListener(this);
		// 新建List
		data_list = new ArrayList<Map<String, Object>>();
		initData();
		return view;
	}

	private void initData() {
		shoppingCarManager = ShoppingCarManager.getInstence();
		mid = Util.getMid();
		HttpUtils httpUtils = new HttpUtils();
		httpUtils.configCurrentHttpCacheExpiry(0);
		httpUtils.send(HttpMethod.GET, ConstantCmd.BASE_URLS + "getshangpingchaxun1.json?Mid=" + mid,
				new RequestCallBack<String>() {
					@Override
					public void onFailure(HttpException arg0, String arg1) {
						if(getActivity() != null) {
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Util.DisplayToast(getActivity(), "网络超时，请重试", R.drawable.warning);
									str2Voice("网络错误，请重试");
								}
							});
						}
					}

					@Override
					public void onSuccess(ResponseInfo<String> arg0) {
						if (!TextUtils.isEmpty(arg0.result)) {
							Util.sendMessage(handler, 0, arg0.result);
						}
					}
				});
	}

	/**
	 * gview子类商品的监听事件跳转
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (Util.isFastClick()) {
			goodsDetailDialog = new GoodsDetailDialog(getActivity(), R.style.MyDialogStyle,
					data_list.get(position).get("Yid").toString());
			if (!getActivity().isDestroyed()) {
				goodsDetailDialog.show();
			}
		}
	}

	class MySimpleAdapter extends SimpleAdapter {
		public List<Map<String, Object>> maps;

		public MySimpleAdapter(List<Map<String, Object>> data, int resource, String[] from, int[] to) {
			super(activity, data, resource, from, to);
			this.maps = data;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			// 购物车图案点击事件
			final ImageView back = (ImageView) view.findViewById(R.id.iv_shoppiing_car_item);
			back.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					// 获取这个被点击的商品的Yid号
					final String goodsYid = maps.get(position).get("Yid").toString();
					for (int i = 0; i < goodsList.size(); i++) {
						// 这是商品列表中的商品
						Goods goodsInList = goodsList.get(i);
						// 判断我们要购买的商品的数量是否已经超过了库存
						if (goodsInList.getYid().equals(goodsYid)) {
							// 获取商品列表中的库存
							int inventory = Integer.parseInt(goodsInList.getZongshu());
							// 如果我们购买的商品的库存数量不足
							if (inventory > 0) {
								try {
									shoppingCarManager.addGoodsToCar(new AlreadyToBuyGoods(goodsInList, 1));
									Toast.makeText(getContext(), "添加购物车成功", Toast.LENGTH_SHORT).show();
									// 将商品添加到购物车中去以后，发送广播，改变购物车上的数量
									sendBroadcast();
									addGoods2CartAnim(back);
								} catch (Exception e) {
									e.printStackTrace();
									// 如果说我们第二次点击这个图标的话，就会报异常；
									Util.DisplayToast(getActivity(), "购物车中已放入该商品", R.drawable.warning);
								}
							} else {
								// 库存数量为零
								Util.DisplayToast(getActivity(), "亲，该商品不能购买更多哦!", R.drawable.warning);
							}
						}
					}
				}
			});
			return view;
		}
	}

	protected void addGoods2CartAnim(ImageView goodsImageView) {
		final ImageView goods = new ImageView(getActivity());
		goods.setImageResource(R.drawable.card_little);
		int size = Util.dp2px(getActivity(), 59);
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(size, size);
		goods.setLayoutParams(lp);
		layout.addView(goods);

		int[] startLocations = new int[2];
		goodsImageView.getLocationInWindow(startLocations);

		int[] recyclerLocation = new int[2];
		layout.getLocationInWindow(recyclerLocation);

		int startX = startLocations[0] - recyclerLocation[0] + goodsImageView.getWidth() / 2;
		int startY = startLocations[1] - recyclerLocation[1] + goodsImageView.getHeight() / 2;

		int endX = endLocations[0] - recyclerLocation[0];
		int endY = endLocations[1] - recyclerLocation[1];

		TranslateAnimation translateAnimationX = new TranslateAnimation(startX, endX, 0, 0);
		translateAnimationX.setInterpolator(new LinearInterpolator());
		translateAnimationX.setRepeatCount(0);
		translateAnimationX.setFillAfter(true);

		TranslateAnimation translateAnimationY = new TranslateAnimation(0, 0, startY, endY);
		translateAnimationY.setInterpolator(new AccelerateInterpolator());
		translateAnimationY.setRepeatCount(0);
		translateAnimationX.setFillAfter(true);

		AnimationSet set = new AnimationSet(false);
		set.setFillAfter(false);
		set.addAnimation(translateAnimationY);
		set.addAnimation(translateAnimationX);
		set.setDuration(800);
		goods.startAnimation(set);
		set.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						layout.removeView(goods);
					}
				});
			}
		});
	}

	private void sendBroadcast() {
		Intent intent = new Intent();
		intent.setAction("getGoodsNumAction");
		getActivity().sendBroadcast(intent);
	}

	private void str2Voice(final String string) {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				if (!TextUtils.isEmpty(string)) {
					VoiceUtils.getInstance().initmTts(string);
				}
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (goodsDetailDialog != null && !getActivity().isDestroyed()) {
			goodsDetailDialog.dismiss();
		}
	}

}
