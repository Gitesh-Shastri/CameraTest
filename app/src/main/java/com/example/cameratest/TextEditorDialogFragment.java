package com.example.cameratest;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedHashMap;
import java.util.List;

import ja.burhanrashid52.photoeditor.GradientDrawableWithColor;

public class TextEditorDialogFragment extends DialogFragment {

    public static final String TAG = TextEditorDialogFragment.class.getSimpleName();
    public static final String EXTRA_INPUT_TEXT = "extra_input_text";
    public static final String EXTRA_COLOR_CODE = "extra_color_code";
    public static final String EXTRA_BACK_COLOR_CODE = "extra_back_color_code";
    private EditText mAddTextEditText;
    TextView change_done_tv;
    private TextView mAddTextDoneTextView;
    private InputMethodManager mInputMethodManager;
   int mColorCode, mBackGroundColor = 1;
    String current_style = "Normal";
    private TextEditor mTextEditor;
    private Typeface mWonderFont,mWonderFont1;
    List<Typeface> mfonts;
    Typeface fonting;
    TextView button_style,highlight_text;
    ImageView iv_back,iv_tick;
    public LinkedHashMap<String, Typeface> fonts;
    int fontcouter =1;
    int fonttypecounter =1;
    SeekBar seekbar_text_size;
    boolean highlight= false;
    int center_counter = 0;
    RelativeLayout relative_background, request_focus_ll;

    Button alignment;



    public interface TextEditor {
        void onDone(String inputText, int colorCode, int mBackGroundColor, String current_style, Typeface font_family, int center_counter);
    }


    //Show dialog with provide text and text color
    public static TextEditorDialogFragment show(@NonNull AppCompatActivity appCompatActivity,
                                                @NonNull String inputText,
                                                @ColorInt int colorCode) {
        Bundle args = new Bundle();
        args.putString(EXTRA_INPUT_TEXT, inputText);
        args.putInt(EXTRA_COLOR_CODE, colorCode);
        args.putInt(EXTRA_BACK_COLOR_CODE, 1);
        TextEditorDialogFragment fragment = new TextEditorDialogFragment();
        fragment.setArguments(args);
        fragment.show(appCompatActivity.getSupportFragmentManager(), TAG);
        return fragment;
    }

    //Show dialog with provide text and text color
    public static TextEditorDialogFragment show(@NonNull AppCompatActivity appCompatActivity,
                                                @NonNull String inputText,
                                                @ColorInt int colorCode,
                                                @ColorInt int backGroundColor) {
        Bundle args = new Bundle();
        args.putString(EXTRA_INPUT_TEXT, inputText);
        args.putInt(EXTRA_COLOR_CODE, colorCode);
        args.putInt(EXTRA_BACK_COLOR_CODE, backGroundColor);
        TextEditorDialogFragment fragment = new TextEditorDialogFragment();
        fragment.setArguments(args);
        fragment.show(appCompatActivity.getSupportFragmentManager(), TAG);
        return fragment;
    }

    //Show dialog with default text input as empty and text color white
    public static TextEditorDialogFragment show(@NonNull AppCompatActivity appCompatActivity) {
        return show(appCompatActivity,
                "", ContextCompat.getColor(appCompatActivity, R.color.white));
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        //Make dialog full screen with transparent background
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_text_dialog, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAddTextEditText = view.findViewById(R.id.add_text_edit_text);

        alignment = view.findViewById(R.id.alignment);
        request_focus_ll = view.findViewById(R.id.request_focus_ll);
        iv_back = view.findViewById(R.id.iv_back);
        highlight_text = view.findViewById(R.id.highlight_text);
        mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        iv_tick = view.findViewById(R.id.iv_tick);
        seekbar_text_size = view.findViewById(R.id.seekbar_text_size);
        change_done_tv  = view.findViewById(R.id.change_done_tv);
        button_style = view.findViewById(R.id.button_style);
        button_style.setTypeface(button_style.getTypeface(), Typeface.NORMAL);
        button_style.setText("Normal");
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        iv_tick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                dismiss();
                String inputText = mAddTextEditText.getText().toString();
                if (!TextUtils.isEmpty(inputText) && mTextEditor != null) {
                    mTextEditor.onDone(inputText, mColorCode, mBackGroundColor, current_style, mWonderFont, center_counter);
                }
            }
        });

        request_focus_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mAddTextEditText.performClick();
                    mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        alignment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                center_counter += 1;
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mAddTextEditText.getLayoutParams();
                if (center_counter == 1){
                    layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
                    alignment.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_format_align_left_24,0, 0, 0);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                } else if (center_counter == 2) {
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                    layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    alignment.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_format_align_right_24,0, 0, 0);
                } else {
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
                    layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    center_counter = 0;
                    alignment.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_format_align_center_24,0, 0, 0);
                }
            }
        });

        button_style.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fonttypecounter++;
                if(fonttypecounter==1){
                    current_style = "Normal";
                    button_style.setText("Normal");
                    mWonderFont = Typeface.createFromAsset(getActivity().getAssets(), "proximanova_regular.otf");
                    mAddTextEditText.setTypeface(mWonderFont);

                }
                else if(fonttypecounter==2){
                    current_style = "Bold";
                    button_style.setText("Bold");
                    mWonderFont = Typeface.createFromAsset(getActivity().getAssets(), "proxima_nova_bold.otf");
                    mAddTextEditText.setTypeface(mWonderFont);

                }
                else if(fonttypecounter==3){
                    current_style = "Italic";
                    button_style.setText("Italic");
                    mAddTextEditText.setTypeface(button_style.getTypeface(), Typeface.ITALIC);
                    fonttypecounter=0;
                }
            }
        });

        highlight_text.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!highlight){
                    highlight_text.setBackgroundResource(R.drawable.highlight_rounded_backgorund);
                    if (mBackGroundColor == 1) {
                        mBackGroundColor = Color.parseColor("#b52828");
                        mColorCode = Color.parseColor("#ffffff");
                    }
                    mAddTextEditText.setTextColor(mColorCode);

                    GradientDrawableWithColor gradientDrawable = new GradientDrawableWithColor();
                    gradientDrawable.setCornerRadius(10);
                    gradientDrawable.setColor(mBackGroundColor);
                    mAddTextEditText.setBackground(gradientDrawable);
                    highlight =true;
                }
                else {
                    highlight =false;
                    mAddTextEditText.setTextColor(mColorCode);
                    highlight_text.setBackgroundResource(R.drawable.rounded_border_text_view);
                    mBackGroundColor = 1;
                    mAddTextEditText.setBackground(null);
                }

            }
        });



        //Setup the color picker for text color
        RecyclerView addTextColorPickerRecyclerView = view.findViewById(R.id.add_text_color_picker_recycler_view);
        RecyclerView fontPickerRecyclerView = view.findViewById(R.id.add_font_familyrecyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        addTextColorPickerRecyclerView.setLayoutManager(layoutManager);
        addTextColorPickerRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManagers = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        fontPickerRecyclerView.setLayoutManager(layoutManagers);
        fontPickerRecyclerView.setHasFixedSize(true);

        seekbar_text_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                System.out.println(progress);
                if(progress>60){
                    mAddTextEditText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);

                }
                if(progress>0 && progress<30){
                    mAddTextEditText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14);
                }
                if(progress>30&&progress<60){
                    mAddTextEditText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                System.out.println("shubham");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println("shubham");
            }
        });

        try {
            mWonderFont = Typeface.createFromAsset(getActivity().getAssets(), "proximanova_regular.otf");
            change_done_tv.setTypeface(mWonderFont);

            change_done_tv.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    fontcouter++;
                    if(fontcouter==1){
                        mWonderFont = Typeface.createFromAsset(getActivity().getAssets(), "beyond_wonderland.ttf");
                        mAddTextEditText.setTypeface(mWonderFont);
                    }
                    else if(fontcouter==2){
                        mWonderFont = Typeface.createFromAsset(getActivity().getAssets(), "proximanova_regular.otf");
                        mAddTextEditText.setTypeface(mWonderFont);
                    }
                    else if(fontcouter==3){
                        mWonderFont = Typeface.createFromAsset(getActivity().getAssets(), "proxima_nova_bold.otf");
                        mAddTextEditText.setTypeface(mWonderFont);
                    }
                    else {
                        mWonderFont = Typeface.createFromAsset(getActivity().getAssets(), "helveticaneuelight.ttf");
                        mAddTextEditText.setTypeface(mWonderFont);
                        fontcouter=1;
                    }
                }
            });


            mWonderFont1 = Typeface.createFromAsset(getActivity().getAssets(), "proximanova_regular.otf");
            mWonderFont1 = Typeface.createFromAsset(getActivity().getAssets(), "proxima_nova_bold.otf");
            fonts = new LinkedHashMap<String, Typeface>();
            fonts.put("Beyond Wonderland",mWonderFont);
            fonts.put("Regular",mWonderFont1);
            FontFamilyAdapter FontPickerAdapter = new FontFamilyAdapter(getActivity(), fonts);
            FontPickerAdapter.setOnColorPickerClickListener(new FontFamilyAdapter.OnColorPickerClickListener() {
                @Override
                public void onColorPickerClickListener(Typeface colorCode) {
                    mAddTextEditText.setTypeface(colorCode);
                }
            });
            fontPickerRecyclerView.setAdapter(FontPickerAdapter);
        }
        catch (Exception e){
            System.out.println(e);
        }
        ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(getActivity());
        //This listener will change the text color when clicked on any color from picker
        colorPickerAdapter.setOnColorPickerClickListener(new ColorPickerAdapter.OnColorPickerClickListener() {
            @Override
            public void onColorPickerClickListener(int colorCode) {
                if (!highlight) {
                    mColorCode = colorCode;
                    mAddTextEditText.setTextColor(colorCode);
                    if (mBackGroundColor == 1) {
                        mAddTextEditText.setBackground(null);
                    } else {
                        GradientDrawableWithColor gradientDrawable = new GradientDrawableWithColor();
                        gradientDrawable.setCornerRadius(10);
                        gradientDrawable.setColor(mBackGroundColor);
                        mAddTextEditText.setBackground(gradientDrawable);
                    }
                } else {
                    try {
                        if (getActivity() != null) {
                            if (ContextCompat.getColor(getActivity(), R.color.white) == colorCode) {
                                mColorCode = ContextCompat.getColor(getActivity(), R.color.black);
                            } else {
                                mColorCode = ContextCompat.getColor(getActivity(), R.color.white);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mBackGroundColor = colorCode;
                    mAddTextEditText.setTextColor(mColorCode);
                    GradientDrawableWithColor gradientDrawable = new GradientDrawableWithColor();
                    gradientDrawable.setCornerRadius(10);
                    gradientDrawable.setColor(mBackGroundColor);
                    mAddTextEditText.setBackground(gradientDrawable);
                }
            }
        });
        addTextColorPickerRecyclerView.setAdapter(colorPickerAdapter);
        mAddTextEditText.setText(getArguments().getString(EXTRA_INPUT_TEXT));
        mColorCode = getArguments().getInt(EXTRA_COLOR_CODE);
        mBackGroundColor = getArguments().getInt(EXTRA_BACK_COLOR_CODE);
        if (mBackGroundColor != 1) {
            GradientDrawableWithColor gradientDrawable = new GradientDrawableWithColor();
            gradientDrawable.setCornerRadius(10);
            gradientDrawable.setColor(mBackGroundColor);
            mAddTextEditText.setBackground(gradientDrawable);
            highlight_text.setBackgroundResource(R.drawable.highlight_rounded_backgorund);
            highlight = true;
        }
        mAddTextEditText.setTextColor(mColorCode);
        mAddTextEditText.requestFocus();
        mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }


    //Callback to listener if user is done with text editing
    public void setOnTextEditorListener(TextEditor textEditor) {
        mTextEditor = textEditor;
    }

}
