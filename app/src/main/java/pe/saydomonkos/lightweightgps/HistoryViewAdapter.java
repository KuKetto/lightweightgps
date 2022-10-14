package pe.saydomonkos.lightweightgps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

public class HistoryViewAdapter extends BaseAdapter {

    private Context context;
    private String[] items;
    private Animation animation;

    public HistoryViewAdapter(Context context, String[] items) {
        this.context = context;
        this.items = items;
    }

    public static int getRandom(int max) {
        return (int) (Math.random() * max);
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(context).inflate(R.layout.history_item_layout, viewGroup, false);
        animation = AnimationUtils.loadAnimation(context, R.anim.animation1);
        TextView textView;
        LinearLayout cardViewData;
        cardViewData = view.findViewById(R.id.card_view_data);
        textView = view.findViewById(R.id.card_view_data_textview);

        int randomNumber = getRandom(8);
        switch (randomNumber) {
            case 1:
                cardViewData.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient_1));
                break;
            case 2:
                cardViewData.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient_2));
                break;
            case 3:
                cardViewData.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient_3));
                break;
            case 4:
                cardViewData.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient_4));
                break;
            case 5:
                cardViewData.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient_5));
                break;
            case 6:
                cardViewData.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient_6));
                break;
            case 7:
                cardViewData.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient_7));
                break;
            case 8:
                cardViewData.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient_8));
                break;
            default:
                cardViewData.setBackground(ContextCompat.getDrawable(context, R.drawable.snow_bg));
                break;
        }

        textView.setText(items[i]);
        textView.setAnimation(animation);

        return view;
    }
}
