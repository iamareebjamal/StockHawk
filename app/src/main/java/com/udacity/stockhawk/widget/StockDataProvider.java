package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.StringUtils;
import com.udacity.stockhawk.data.Contract;

public class StockDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private Intent intent;
    private Cursor cursor;

    public StockDataProvider(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
    }

    private void loadData() {
        if (cursor != null) {
            cursor.close();
        }

        final long identityToken = Binder.clearCallingIdentity();

        cursor = context.getContentResolver().query(
                Contract.Quote.uri,
                Contract.Quote.QUOTE_COLUMNS,
                null,
                null,
                Contract.Quote.COLUMN_SYMBOL);

        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onCreate() {
        loadData();
        if (cursor != null && cursor.getCount() > 0) cursor.moveToFirst();

    }

    @Override
    public void onDataSetChanged() {
        loadData();
    }

    @Override
    public void onDestroy() {
        cursor.close();
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.list_item_quote);
        cursor.moveToPosition(i);

        String symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);
        remoteViews.setTextViewText(R.id.symbol, symbol);
        remoteViews.setTextViewText(R.id.price, StringUtils.formatPrice(cursor.getFloat(Contract.Quote.POSITION_PRICE)));

        float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);

        if (rawAbsoluteChange > 0) {
            remoteViews.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
        } else {
            remoteViews.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }

        remoteViews.setTextViewText(R.id.change, StringUtils.formatAbsoluteChange(rawAbsoluteChange));

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(Contract.Quote.COLUMN_SYMBOL, symbol);
        remoteViews.setOnClickFillInIntent(R.id.root_view, fillInIntent);

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
