package tv.piratemedia.myBarista.ui.settings.pref;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.R.attr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import tv.piratemedia.myBarista.R;

public final class FloatSeekBarPreference extends Preference implements OnSeekBarChangeListener {
    private final float minValue;
    private final float maxValue;
    private final float stepSize;
    private final String format;
    private SeekBar seekbar;
    private TextView textView;
    private float defaultValue;
    private float newValue;

    public FloatSeekBarPreference(@NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        this.setWidgetLayoutResource(1300003);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FloatSeekBarPreference, defStyleAttr, defStyleRes);
        this.minValue = ta.getFloat(R.styleable.FloatSeekBarPreference_minValue, 0.0F);
        this.maxValue = ta.getFloat(R.styleable.FloatSeekBarPreference_maxValue, 1.0F);
        this.stepSize = ta.getFloat(R.styleable.FloatSeekBarPreference_stepSize, 0.1F);
        this.defaultValue = ta.getFloat(R.styleable.FloatSeekBarPreference_android_defaultValue, 3.0f);
        String fmt = ta.getString(R.styleable.FloatSeekBarPreference_format);
        if (fmt == null) {
            fmt = "%3.1f";
        }

        this.format = fmt;
        ta.recycle();
    }

    public FloatSeekBarPreference(@NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FloatSeekBarPreference(@NotNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, attr.seekBarPreferenceStyle);
    }

    public FloatSeekBarPreference(@NotNull Context context) {
        this(context, null);
    }

    public float getValue() {
        return this.seekbar != null ? ((float) this.seekbar.getProgress() * this.stepSize + this.minValue) : this.defaultValue;
    }

    public void setValue(float v) {
        this.newValue = v;
        this.persistFloat(v);
        this.notifyChanged();
    }

    @Override
    protected void onSetInitialValue(Object defVal) {
        if (defVal == null) {
            defVal = 0f;
        }

        if(defVal.getClass().getName().equals("java.lang.Integer")) {
            int dVal = (int)defVal;
            setValue(getPersistedFloat((float) dVal));
        }
        setValue(getPersistedFloat((float) defVal));

    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getFloat(index, 0);
    }

    public void onBindViewHolder(@Nullable PreferenceViewHolder holder) {
        assert holder != null;
        super.onBindViewHolder(holder);

        holder.itemView.setClickable(false);

        this.seekbar = (SeekBar) holder.findViewById(R.id.seekbar);
        this.textView = (TextView) holder.findViewById(R.id.seekbar_value);

        this.seekbar.setOnSeekBarChangeListener(this);

        this.seekbar.setMax((int)((this.maxValue - this.minValue) / this.stepSize));
        this.seekbar.setProgress((int)((this.newValue - this.minValue) / this.stepSize));
        this.seekbar.setEnabled(this.isEnabled());

        this.textView.setText(String.format(this.format, this.newValue));
    }

    public void onProgressChanged(@Nullable SeekBar seekbar, int progress, boolean fromUser) {
        if (fromUser) {
            float v = this.minValue + (float)progress * this.stepSize;
            this.textView.setText(String.format(format, v));
        }
    }

    public void onStartTrackingTouch(@Nullable SeekBar seekbar) {
    }

    public void onStopTrackingTouch(@Nullable SeekBar seekbar) {
        assert seekbar != null;
        float v = this.minValue + (float) seekbar.getProgress() * this.stepSize;
        this.persistFloat(v);
    }
}