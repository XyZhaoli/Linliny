package android_serialport_api;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android_serialport_api.sample.R;

public class MyAdapter extends BaseAdapter {

	private Context context;
	private List<PersionInfo> listinfos;

	public MyAdapter(Context context, List<PersionInfo> listinfos) {
		this.context = context;
		this.listinfos = listinfos;
	}

	@Override
	public int getCount() {
		return listinfos.size();
	}

	@Override
	public Object getItem(int position) {
		return listinfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		convertView = LayoutInflater.from(context).inflate(R.layout.list_item, null);
		TextView tv = (TextView) convertView.findViewById(R.id.tv);
		PersionInfo persionInfo = listinfos.get(position);
		tv.setText(persionInfo.getNameString());

		if (persionInfo.isChick()) {
			convertView.setBackgroundColor(Color.parseColor("#36b010"));
			tv.setTextColor(Color.parseColor("#000000"));
		} else {
			convertView.setBackgroundColor(Color.parseColor("#ffffff"));
			tv.setTextColor(Color.parseColor("#A1A1A1"));
		}
		return convertView;
	}
}