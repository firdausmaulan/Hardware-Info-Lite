package nutech.hardware.info;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView; 
import java.util.ArrayList;
 
public class CustomListView extends BaseAdapter {
    // params
    ArrayList<ListContent> listItem;
    Activity activity;
 
    public CustomListView(Activity activity, ArrayList<ListContent> listItem){
        this.activity = activity;
        this.listItem = listItem;
    }

    public int getCount() {
        return listItem.size();
    }
 
    //method ini untuk mengakses per-item objek dalam list
    public Object getItem(int position) {
        return listItem.get(position);
    }
 
    public long getItemId(int position) {
        return 0;
    }
 
    //method ini yang akan menampilkan baris per baris dari item yang ada di ListView
    //dengan menggunakan pattern ViewHolder untuk optimasi performa dari ListView
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder = null;
 
        if (view == null){
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_content, null);
            holder.txtLeft = (TextView)view.findViewById(R.id.txtLeft);
            holder.txtRight = (TextView)view.findViewById(R.id.txtRight);
            view.setTag(holder);
        }else{
            holder = (ViewHolder)view.getTag();
        }
 
        ListContent inside = (ListContent)getItem(position);
        holder.txtLeft.setText(inside.getLeft());
        holder.txtRight.setText(inside.getRight());
        return view;
    }
 
    static class ViewHolder{
        TextView txtLeft, txtRight;
    }
}