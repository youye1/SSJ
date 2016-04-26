package com.hzm.lczs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import com.hzm.lczs.db.MyDbHelper;
import com.hzm.lczs.db.MyDbInfo;
import com.hzm.lczs.R;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.Toast;

/**
 * 流水账列表Activity
 * 今天 本周  本月  流水帐
 */
public class NavExpenseActivity extends Activity
        implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    public static String str_startTime = "startTime";
    public static String str_endTime = "endTime";
    public static String str_title = "title";
    public static String str_mode = "mode";
    public static final int mode_none = 0;
    public static final int mode_month = 1;
    public static final int mode_week = 2;
    public static final int mode_day = 3;
    private static String datefmt = "yyyy年MM月dd日";
    CommonData commondata = CommonData.getInstance();
    private TextView title_tv;
    private TextView time_interval_tv;
    public ListView expense_lv;
    private View empty_tips;

    public long start_time, end_time;
    private String title;
    private int mode;


    public MyDbHelper db = null;//数据库辅助类

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_expense_activity);
        db = SplashScreenActivity.db;

        title_tv = (TextView) findViewById(R.id.title_tv);
        time_interval_tv = (TextView) findViewById(R.id.time_interval_tv);
        expense_lv = (ListView) findViewById(R.id.expense_lv);

        //加载布局文件
        empty_tips = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.common_lv_empty_tips, null);

        findViewById(R.id.pre_btn).setOnClickListener(this);
        findViewById(R.id.next_btn).setOnClickListener(this);
        expense_lv.setOnItemClickListener(this);
        expense_lv.setOnItemLongClickListener(this);

        Intent intent = getIntent();
        start_time = intent.getLongExtra(str_startTime, 0);
        end_time = intent.getLongExtra(str_endTime, 0);
        title = intent.getStringExtra(str_title);
        mode = intent.getIntExtra(str_mode, mode_none);

        if (start_time == 0 || end_time == 0 || TextUtils.isEmpty(title) || mode == mode_none) {
            Toast.makeText(this, getString(R.string.error_system_message), Toast.LENGTH_SHORT).show();
            finish();
        }

        setTimeIntervalText();

        expense_lv.setEmptyView(empty_tips);
        title_tv.setText(title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            refreshTransactions();
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        refreshTransactions();
    }

    @Override
    public void onClick(View v) {
        Date date1, date2;
        switch (v.getId()) {
            //左边按钮(前段时间)
            case R.id.pre_btn:
                switch (mode) {
                    case mode_day:
                        date1 = new Date(start_time);
                        date1.setDate(date1.getDate() - 1);
                        start_time = end_time = date1.getTime();
                        break;
                    case mode_week:
                        date1 = new Date(start_time);
                        date2 = new Date(end_time);
                        date1.setDate(date1.getDate() - 7);
                        date2.setDate(date2.getDate() - 7);
                        start_time = date1.getTime();
                        end_time = date2.getTime();
                        break;
                    case mode_month:
                        date1 = new Date(start_time);
                        date2 = new Date(end_time);
                        date1 = new Date(date1.getYear(), date1.getMonth() - 1, 1);
                        date2 = new Date(date2.getYear(), date2.getMonth(), 0);
                        start_time = date1.getTime();
                        end_time = date2.getTime();
                        break;
                }
                break;
            //右边按钮（后段时间）
            case R.id.next_btn:
                switch (mode) {
                    case mode_day:
                        date1 = new Date(start_time);
                        date1.setDate(date1.getDate() + 1);
                        start_time = end_time = date1.getTime();
                        break;
                    case mode_week:
                        date1 = new Date(start_time);
                        date2 = new Date(end_time);
                        date1.setDate(date1.getDate() + 7);
                        date2.setDate(date2.getDate() + 7);
                        start_time = date1.getTime();
                        end_time = date2.getTime();
                        break;
                    case mode_month:
                        date1 = new Date(start_time);
                        date2 = new Date(end_time);
                        date1 = new Date(date1.getYear(), date1.getMonth() + 1, 1);
                        date2 = new Date(date2.getYear(), date2.getMonth() + 2, 0);
                        start_time = date1.getTime();
                        end_time = date2.getTime();
                        break;
                }
                break;
        }

        setTimeIntervalText();
        refreshTransactions();//加载收支使用情况
    }

    @Override//弹出的删除与编辑  对话框
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        class NavItemLongClickListener implements DialogInterface.OnClickListener {
            NavExpenseActivity nav;
            TransactionData data;

            public NavItemLongClickListener(NavExpenseActivity nav, TransactionData data) {
                this.nav = nav;
                this.data = data;
            }

            public void onClick(DialogInterface dialog, int which) {
                if (data != null) {
                    if (which == 0) { //编辑
                        Intent intent = new Intent(nav, TransactionTabActivity.class);
                        intent.putExtra("mode", TransactionTabActivity.EDIT_MODE);
                        Bundle mBundle = new Bundle();
                        mBundle.putParcelable("data", data);
                        intent.putExtras(mBundle);
//						intent.putExtra(str_mode, mode);
                        nav.startActivityForResult(intent, 0);
                    } else {//删除
                        AlertDialog.Builder builder = new AlertDialog.Builder(nav);
                        builder.setTitle(R.string.delete_title);
                        builder.setMessage(R.string.message_delete);

                        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                nav.deleteTransaction(data);//删除收支项目
                                Toast.makeText(nav, getString(R.string.message_delete_ok), Toast.LENGTH_SHORT).show();
                            }
                        });

                        builder.setNegativeButton(R.string.delete_cancel, null);

                        builder.create().show();
                    }
                } else {
                    Toast.makeText(nav, getString(R.string.message_error_edit), Toast.LENGTH_SHORT).show();
                }
            }
        }

        TransactionData data = (TransactionData) view.getTag();

        AlertDialog.Builder builder = new AlertDialog.Builder(this); //编辑删除对话框
        builder.setItems(R.array.setting_listview_item_operation, new NavItemLongClickListener(this, data));

        builder.create().show();

        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TransactionData data = (TransactionData) view.getTag();
        if (data != null) {
            Intent intent = new Intent(this, TransactionTabActivity.class);
            intent.putExtra("mode", TransactionTabActivity.EDIT_MODE);
            intent.putExtra("data", data);
            startActivityForResult(intent, 0);
        }
    }

    /*
     *设置账单List页面日期显示格式
     */
    private void setTimeIntervalText() {
        if (start_time == end_time) {
            SimpleDateFormat sdf = new SimpleDateFormat(datefmt);
            time_interval_tv.setText(sdf.format(new Date(start_time)));
        } else {
            Date date1 = new Date(start_time);
            Date date2 = new Date(end_time);
            SimpleDateFormat sdf = new SimpleDateFormat(datefmt);
            time_interval_tv.setText(sdf.format(date1) + "-" + sdf.format(date2));
        }
    }

    /**
     * 使用异步任务类加载收支情况
     */
    private void refreshTransactions() {
        new TransactionListAsyncTask().execute(this);
    }

    /**
     * 更新收支情况
     * @param tdata
     */
    private void updataAccount(TransactionData tdata) {
        Iterator<AccountData> iteratorSort = commondata.account.values().iterator();
        while (iteratorSort.hasNext()) {
            AccountData data = iteratorSort.next();
            if (data.id == tdata.account_id) {
                if (tdata.type == 0) {//收入
                    data.balance = data.balance - tdata.money;
                    commondata.updateAccount(data);
                } else if (tdata.type == 1) {//支出
                    data.balance = data.balance + tdata.money;
                    commondata.updateAccount(data);
                }
                return;
            }
        }

    }

    /**
     * 删除收支项目
     *
     * @param data
     */
    public void deleteTransaction(TransactionData data) {
        int id = 0;
        if (data.type == 0) {//收入
            id = 10;
        } else {//支出
            id = 9;
        }
        db.delete(MyDbInfo.getTableNames()[id], "ID=?", new String[]{String.valueOf(data.infoId)});
        updataAccount(data);
        refreshTransactions();
    }
}
