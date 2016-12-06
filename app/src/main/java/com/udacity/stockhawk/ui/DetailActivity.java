package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.StringUtils;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int STOCK_LOADER = 0;
    @BindView(R.id.linechart)
    LineChartView lineChartView;
    @BindView(R.id.symbol)
    TextView symbol;
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.price)
    TextView price;
    @BindView(R.id.absoluteChange)
    TextView absoluteChange;
    @BindView(R.id.percentageChange)
    TextView percentageChange;

    private Paint gridPaint;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        String symbol = getIntent().getStringExtra(Contract.Quote.COLUMN_SYMBOL);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Bundle bundle = new Bundle();
        bundle.putString(
                Contract.Quote.COLUMN_SYMBOL,
                symbol
        );

        getSupportLoaderManager().initLoader(STOCK_LOADER, bundle, this);

        gridPaint = new Paint();
        gridPaint.setColor(ContextCompat.getColor(this, R.color.material_purple_light));
        gridPaint.setStrokeWidth(5);
    }

    private void handleHistoryData(String history) {
        lineChartView.dismiss();
        lineChartView.setXAxis(false);
        lineChartView.setYAxis(false);
        lineChartView.setYLabels(AxisController.LabelPosition.NONE);
        lineChartView.setXLabels(AxisController.LabelPosition.NONE);
        lineChartView.setGrid(ChartView.GridType.FULL, gridPaint);

        LineSet lineSet = new LineSet();
        lineSet.setSmooth(true);
        lineSet.setColor(ContextCompat.getColor(this, R.color.material_purple));
        lineSet.setFill(ContextCompat.getColor(this, R.color.material_purple_trans));

        String[] datedStocks = history.split("\n");
        for (String datedStock : datedStocks) {
            String[] stockData = datedStock.split(",");

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(stockData[0].trim()));

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            String date = sdf.format(calendar.getTime());
            Float stock = Float.parseFloat(stockData[1].trim());

            lineSet.addPoint(date, stock);
        }

        lineChartView.addData(lineSet);
        lineChartView.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case STOCK_LOADER:
                if (args == null || args.getString(Contract.Quote.COLUMN_SYMBOL) == null)
                    return null;

                return new CursorLoader(
                        this,
                        Contract.Quote.makeUriForStock(args.getString(Contract.Quote.COLUMN_SYMBOL)),
                        null,
                        null,
                        null,
                        null
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        handleHistoryData(data.getString(Contract.Quote.POSITION_HISTORY));

        String stockName = data.getString(Contract.Quote.POSITION_NAME);
        String stockSymbol = data.getString(Contract.Quote.POSITION_SYMBOL);
        float stockPrice = data.getFloat(Contract.Quote.POSITION_PRICE);
        float stockAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float stockPercentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        if (actionBar != null) actionBar.setTitle(stockName);

        symbol.setText(stockSymbol);
        name.setText(stockName);
        price.setText(StringUtils.formatPrice(stockPrice));
        absoluteChange.setText(StringUtils.formatAbsoluteChange(stockAbsoluteChange));
        percentageChange.setText(StringUtils.formatPercentageChange(stockPercentageChange / 100));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
