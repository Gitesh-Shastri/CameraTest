package com.example.cameratest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class FontFamilyAdapter extends RecyclerView.Adapter<FontFamilyAdapter.ViewHolder> {

    private Context context;
    Typeface mWonderFont;
    private LayoutInflater inflater;
    private LinkedHashMap<String, Typeface> colorPickerColors;
    private OnColorPickerClickListener onColorPickerClickListener;

    public FontFamilyAdapter(@NonNull Context context, @NonNull LinkedHashMap<String, Typeface> colorPickerColors) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.colorPickerColors = colorPickerColors;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.font_family_item_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
      //  holder.colorPickerView.setBackgroundColor(colorPickerColors.get(position));
      Object  keyset = (new ArrayList<>(colorPickerColors.keySet())).get(position);
        final Typeface value=(Typeface)colorPickerColors.get(keyset);
        holder.fontView.setTypeface(value);
        holder.fontView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onColorPickerClickListener != null);
                onColorPickerClickListener.onColorPickerClickListener(value);
            }
        });
        // mWonderFont = Typeface.createFromAsset(context.getAssets(), colorPickerColors.get(position).toString());
        //holder.fontView.setText("");
    }

    @Override
    public int getItemCount() {
        return colorPickerColors.size();
    }

    private void buildColorPickerView(View view, int colorCode) {
        view.setVisibility(View.VISIBLE);

        ShapeDrawable biggerCircle = new ShapeDrawable(new OvalShape());
        biggerCircle.setIntrinsicHeight(20);
        biggerCircle.setIntrinsicWidth(20);
        biggerCircle.setBounds(new Rect(0, 0, 20, 20));
        biggerCircle.getPaint().setColor(colorCode);

        ShapeDrawable smallerCircle = new ShapeDrawable(new OvalShape());
        smallerCircle.setIntrinsicHeight(5);
        smallerCircle.setIntrinsicWidth(5);
        smallerCircle.setBounds(new Rect(0, 0, 5, 5));
        smallerCircle.getPaint().setColor(Color.WHITE);
        smallerCircle.setPadding(10, 10, 10, 10);
        Drawable[] drawables = {smallerCircle, biggerCircle};

        LayerDrawable layerDrawable = new LayerDrawable(drawables);

        view.setBackgroundDrawable(layerDrawable);
    }

    public void setOnColorPickerClickListener(OnColorPickerClickListener onColorPickerClickListener) {
        this.onColorPickerClickListener = onColorPickerClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
       TextView fontView;

        public ViewHolder(View itemView) {
            super(itemView);
            fontView = itemView.findViewById(R.id.font_view);

        }
    }

    public interface OnColorPickerClickListener {
        void onColorPickerClickListener(Typeface colorCode);
    }



    public static List<Integer> getDefaultColors(Context context) {
        ArrayList<Integer> colorPickerColors = new ArrayList<>();
        colorPickerColors.add(ContextCompat.getColor(context, R.color.blue_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.brown_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.green_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.orange_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.red_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.black));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.red_orange_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.sky_blue_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.violet_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.white));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.yellow_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.yellow_green_color_picker));
        return colorPickerColors;
    }
}

