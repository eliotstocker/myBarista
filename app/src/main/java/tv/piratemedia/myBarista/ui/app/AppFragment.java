package tv.piratemedia.myBarista.ui.app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

import it.sephiroth.android.library.numberpicker.NumberPicker;
import tv.piratemedia.myBarista.R;
import tv.piratemedia.myBarista.databinding.FragmentAppBinding;

public class AppFragment extends Fragment {

    private FragmentAppBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AppViewModel appViewModel =
                new ViewModelProvider(this).get(AppViewModel.class);

        binding = FragmentAppBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final View mainUI = binding.mainUi;
        final LinearLayout statusUI = binding.statusUi;
        final TextView powerText = binding.powerIndicatorText;
        final ProgressBar powerProgress = binding.powerIndicator;
        final LineChart historyChart = binding.history;
        final TextView currentTemp = binding.currentTemp;
        final NumberPicker setpointPicker = binding.setpointPicker;
        final TextView progressText = binding.progressText;
        final TextView infoText = binding.infoText;
        final LinearLayout shotTimer = binding.shotTimer;
        final TextView shotTimerMain = binding.shotTimerMain;
        final TextView shotTimerSecondary = binding.shotTimerSecondary;
        final Button shotTimerClose = binding.shotTimerClose;

        styleChart(historyChart);

        appViewModel.getCurrentStatusVisibility().observe(getViewLifecycleOwner(), statusUI::setVisibility);
        appViewModel.getCurrentUIVisibility().observe(getViewLifecycleOwner(), mainUI::setVisibility);

        appViewModel.getStatusProgress().observe(getViewLifecycleOwner(), progressText::setText);
        appViewModel.getStatusInfo().observe(getViewLifecycleOwner(), infoText::setText);
        appViewModel.getPowerDisplay().observe(getViewLifecycleOwner(), powerText::setText);
        appViewModel.getPower().observe(getViewLifecycleOwner(), powerProgress::setProgress);
        appViewModel.getCurrentTemp().observe(getViewLifecycleOwner(), currentTemp::setText);
        appViewModel.getCurrentSetpoint().observe(getViewLifecycleOwner(), setpointPicker::setProgress);
        appViewModel.getTempHistory().observe(getViewLifecycleOwner(), lineData -> {
            historyChart.setData(lineData);
            historyChart.notifyDataSetChanged();
            historyChart.invalidate();
        });
        appViewModel.getMinChartValue().observe(getViewLifecycleOwner(), min -> historyChart.getAxisLeft().setAxisMinimum(min));
        appViewModel.getShotTimerVisibility().observe(getViewLifecycleOwner(), shotTimer::setVisibility);
        appViewModel.getShotTimerSecondaryVisibility().observe(getViewLifecycleOwner(), shotTimerSecondary::setVisibility);
        appViewModel.getShotTimerSecondaryValue().observe(getViewLifecycleOwner(), shotTimerSecondary::setText);
        appViewModel.getShotTimerValue().observe(getViewLifecycleOwner(), shotTimerMain::setText);

        shotTimerClose.setOnClickListener(AppViewModel::hideTimer);

        return root;
    }

    private void styleChart(LineChart chart) {
        chart.setDrawBorders(false);
        chart.getDescription().setText("");
        chart.getLegend().setEnabled(false);
        chart.setMaxVisibleValueCount(100);
        XAxis xAxis = chart.getXAxis();
        YAxis yAxis1 = chart.getAxisLeft();
        YAxis yAxis2 = chart.getAxisRight();
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawLabels(false);
        yAxis1.setDrawAxisLine(false);
        yAxis2.setDrawAxisLine(false);
        yAxis2.setDrawLabels(false);
        yAxis1.setDrawGridLines(false);
        yAxis1.setAxisMinimum(30);
        yAxis1.setTextColor(getResources().getColor(R.color.chart_labels, requireActivity().getTheme()));
        yAxis2.setGridColor(Color.parseColor("#337f7f7f"));
        yAxis2.setDrawGridLinesBehindData(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}