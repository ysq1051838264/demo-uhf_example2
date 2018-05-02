package com.example.uhf.fragment;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uhf.R;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.tools.StringUtils;
import com.example.uhf.tools.UIHelper;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.rscja.deviceapi.RFIDWithUHF;

import java.util.ArrayList;
import java.util.HashMap;


public class UHFReadTagFragment extends KeyDwonFragment {

    private boolean loopFlag = false;
    private int inventoryFlag = 1;
    Handler handler;
    private ArrayList<HashMap<String, String>> tagList;
    SimpleAdapter adapter;
    Button BtClear;
    TextView tv_count;
    RadioGroup RgInventory;
    RadioButton RbInventorySingle;
    RadioButton RbInventoryLoop;

    Button BtInventory;
    ListView LvTags;
    private Button btnFilter;//过滤
    private LinearLayout llContinuous;
    private UHFMainActivity mContext;
    private HashMap<String, String> map;
    PopupWindow popFilter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("MY", "UHFReadTagFragment.onCreateView");
        return inflater
                .inflate(R.layout.uhf_readtag_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i("MY", "UHFReadTagFragment.onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        mContext = (UHFMainActivity) getActivity();
        tagList = new ArrayList<HashMap<String, String>>();
        BtClear = (Button) getView().findViewById(R.id.BtClear);
        tv_count = (TextView) getView().findViewById(R.id.tv_count);
        RgInventory = (RadioGroup) getView().findViewById(R.id.RgInventory);
        String tr = "";
        RbInventorySingle = (RadioButton) getView()
                .findViewById(R.id.RbInventorySingle);
        RbInventoryLoop = (RadioButton) getView()
                .findViewById(R.id.RbInventoryLoop);

        BtInventory = (Button) getView().findViewById(R.id.BtInventory);
        LvTags = (ListView) getView().findViewById(R.id.LvTags);

        llContinuous = (LinearLayout) getView().findViewById(R.id.llContinuous);

        adapter = new SimpleAdapter(mContext, tagList, R.layout.listtag_items,
                new String[]{"tagUii", "tagLen", "tagCount", "tagRssi"},
                new int[]{R.id.TvTagUii, R.id.TvTagLen, R.id.TvTagCount,
                        R.id.TvTagRssi});

        //设置开启tid
        mContext.mReader.setEPCTIDMode(true);


        BtClear.setOnClickListener(new BtClearClickListener());
        RgInventory.setOnCheckedChangeListener(new RgInventoryCheckedListener());
        BtInventory.setOnClickListener(new BtInventoryClickListener());
        btnFilter = (Button) getView().findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popFilter == null) {
                    View viewPop = LayoutInflater.from(mContext).inflate(R.layout.popwindow_filter, null);

                    popFilter = new PopupWindow(viewPop, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, true);

                    popFilter.setTouchable(true);
                    popFilter.setOutsideTouchable(true);
                    popFilter.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    popFilter.setBackgroundDrawable(new BitmapDrawable());

                    final EditText etLen = (EditText) viewPop.findViewById(R.id.etLen);
                    final EditText etPtr = (EditText) viewPop.findViewById(R.id.etPtr);
                    final EditText etData = (EditText) viewPop.findViewById(R.id.etData);
                    final RadioButton rbEPC = (RadioButton) viewPop.findViewById(R.id.rbEPC);
                    final RadioButton rbTID = (RadioButton) viewPop.findViewById(R.id.rbTID);
                    final RadioButton rbUser = (RadioButton) viewPop.findViewById(R.id.rbUser);
                    final Button btSet = (Button) viewPop.findViewById(R.id.btSet);

                    btSet.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String filterBank = "UII";
                            if (rbEPC.isChecked()) {
                                filterBank = "UII";
                            } else if (rbTID.isChecked()) {
                                filterBank = "TID";
                            } else if (rbUser.isChecked()) {
                                filterBank = "USER";
                            }
                            if (etLen.getText().toString() == null || etLen.getText().toString().isEmpty()) {
                                UIHelper.ToastMessage(mContext, "数据长度不能为空");
                                return;
                            }
                            if (etPtr.getText().toString() == null || etPtr.getText().toString().isEmpty()) {
                                UIHelper.ToastMessage(mContext, "起始地址不能为空");
                                return;
                            }
                            int ptr = StringUtils.toInt(etPtr.getText().toString(), 0);
                            int len = StringUtils.toInt(etLen.getText().toString(), 0);
                            String data = etData.getText().toString().trim();
                            if (len > 0) {
                                String rex = "[\\da-fA-F]*"; //匹配正则表达式，数据为十六进制格式
                                if (data == null || data.isEmpty() || !data.matches(rex)) {
                                    UIHelper.ToastMessage(mContext, "过滤的数据必须是十六进制数据");
//									mContext.playSound(2);
                                    return;
                                }

                                if (mContext.mReader.setFilter(RFIDWithUHF.BankEnum.valueOf(filterBank), ptr, len, data, false)) {
                                    UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_filter_succ);
                                } else {
                                    UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_filter_fail);
//									mContext.playSound(2);
                                }
                            } else {
                                //禁用过滤
                                String dataStr = "";
                                if (mContext.mReader.setFilter(RFIDWithUHF.BankEnum.valueOf("UII"), 0, 0, dataStr, false)
                                        && mContext.mReader.setFilter(RFIDWithUHF.BankEnum.valueOf("TID"), 0, 0, dataStr, false)
                                        && mContext.mReader.setFilter(RFIDWithUHF.BankEnum.valueOf("USER"), 0, 0, dataStr, false)) {
                                    UIHelper.ToastMessage(mContext, R.string.msg_disable_succ);
                                } else {
                                    UIHelper.ToastMessage(mContext, R.string.msg_disable_fail);
                                }
                            }


                        }
                    });
                    CheckBox cb_filter = (CheckBox) viewPop.findViewById(R.id.cb_filter);
                    rbEPC.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (rbEPC.isChecked()) {
                                etPtr.setText("32");
                            }
                        }
                    });
                    rbTID.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (rbTID.isChecked()) {
                                etPtr.setText("0");
                            }
                        }
                    });
                    rbUser.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (rbUser.isChecked()) {
                                etPtr.setText("0");
                            }
                        }
                    });

                    cb_filter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) { //启用过滤

                            } else { //禁用过滤

                            }
                            popFilter.dismiss();
                        }
                    });
                }
                if (popFilter.isShowing()) {
                    popFilter.dismiss();
                    popFilter = null;
                } else {
                    popFilter.showAsDropDown(view);
                }
            }
        });
        LvTags.setAdapter(adapter);
        clearData();
        Log.i("MY", "UHFReadTagFragment.EtCountOfTags=" + tv_count.getText());
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String result = msg.obj + "";
                String[] strs = result.split("@");
                addEPCToList(strs[0], strs[1]);
                mContext.playSound(1);
            }
        };
    }

    @Override
    public void onPause() {
        Log.i("MY", "UHFReadTagFragment.onPause");
        super.onPause();

        // 停止识别
        stopInventory();
    }

    /**
     * 添加EPC到列表中
     *
     * @param epc
     */
    private void addEPCToList(String epc, String rssi) {
        if (!TextUtils.isEmpty(epc)) {

            //			int index = checkIsExist(epc);

            //改用tid判断
            String[] s = epc.split("\n");
            int index = checkIsTIDExist(s[0]);

            map = new HashMap<String, String>();
            map.put("tagTID", s[0]);


            map.put("tagUii", epc);
            map.put("tagCount", String.valueOf(1));
            map.put("tagRssi", rssi);

            // mContext.getAppContext().uhfQueue.offer(epc + "\t 1");

            if (index == -1) {
                tagList.add(map);
                LvTags.setAdapter(adapter);
                tv_count.setText("" + adapter.getCount());
            } else {
                int tagcount = Integer.parseInt(
                        tagList.get(index).get("tagCount"), 10) + 1;

                map.put("tagCount", String.valueOf(tagcount));

                tagList.set(index, map);

            }

            adapter.notifyDataSetChanged();

        }
    }

    public class BtClearClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            clearData();

        }
    }

    private void clearData() {
        tv_count.setText("0");

        tagList.clear();

        Log.i("MY", "tagList.size " + tagList.size());

        adapter.notifyDataSetChanged();
    }

    public class RgInventoryCheckedListener implements OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            llContinuous.setVisibility(View.GONE);
            if (checkedId == RbInventorySingle.getId()) {
                // 单步识别
                inventoryFlag = 0;
            } else if (checkedId == RbInventoryLoop.getId()) {
                // 单标签循环识别
                inventoryFlag = 1;
                llContinuous.setVisibility(View.VISIBLE);
            }
        }
    }


    public class BtInventoryClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            readTag();
        }
    }

    private void readTag() {
        if (BtInventory.getText().equals(
                mContext.getString(R.string.btInventory)))// 识别标签
        {
            switch (inventoryFlag) {
                case 0:// 单步
                {
                    String strUII = mContext.mReader.inventorySingleTag();
                    if (!TextUtils.isEmpty(strUII)) {
                        String strEPC = mContext.mReader.convertUiiToEPC(strUII);
                        addEPCToList(strEPC, "N/A");
                        tv_count.setText("" + adapter.getCount());
                    } else {
                        UIHelper.ToastMessage(mContext, R.string.uhf_msg_inventory_fail);
//					mContext.playSound(2);
                    }
                }
                break;
                case 1:// 单标签循环
                {
                    if (mContext.mReader.startInventoryTag((byte) 0, (byte) 0)) {
                        BtInventory.setText(mContext
                                .getString(R.string.title_stop_Inventory));
                        loopFlag = true;
                        setViewEnabled(false);
                        new TagThread().start();
                    } else {
                        mContext.mReader.stopInventory();
                        UIHelper.ToastMessage(mContext,
                                R.string.uhf_msg_inventory_open_fail);
//					mContext.playSound(2);
                    }
                }
                break;
                default:
                    break;
            }
        } else {// 停止识别
            stopInventory();
        }
    }

    private void setViewEnabled(boolean enabled) {
        RbInventorySingle.setEnabled(enabled);
        RbInventoryLoop.setEnabled(enabled);
        btnFilter.setEnabled(enabled);
        BtClear.setEnabled(enabled);
    }

    /**
     * 停止识别
     */
    private void stopInventory() {
        if (loopFlag) {
            loopFlag = false;
            setViewEnabled(true);
            if (mContext.mReader.stopInventory()) {
                BtInventory.setText(mContext.getString(R.string.btInventory));
            } else {
                UIHelper.ToastMessage(mContext,
                        R.string.uhf_msg_inventory_stop_fail);
            }

        }
    }

    /**
     * 判断Tid是否在列表中
     *
     * @return
     */
    public int checkIsTIDExist(String tid) {
        int existFlag = -1;
        if (StringUtils.isEmpty(tid)) {
            return existFlag;
        }
        String tempStr = "";
        for (int i = 0; i < tagList.size(); i++) {
            HashMap<String, String> temp = new HashMap<String, String>();
            temp = tagList.get(i);
            tempStr = temp.get("tagTID");
            if (tid.equals(tempStr)) {
                existFlag = i;
                break;
            }
        }
        return existFlag;
    }


    /**
     * 判断EPC是否在列表中
     *
     * @param strEPC 索引
     * @return
     */
    public int checkIsExist(String strEPC) {
        int existFlag = -1;
        if (StringUtils.isEmpty(strEPC)) {
            return existFlag;
        }
        String tempStr = "";
        for (int i = 0; i < tagList.size(); i++) {
            HashMap<String, String> temp = new HashMap<String, String>();
            temp = tagList.get(i);
            tempStr = temp.get("tagUii");
            if (strEPC.equals(tempStr)) {
                existFlag = i;
                break;
            }
        }
        return existFlag;
    }

    class TagThread extends Thread {
        public void run() {
            String strTid;
            String strResult;
            String[] res = null;
            while (loopFlag) {
                res = mContext.mReader.readTagFromBuffer();
                if (res != null) {
                    strTid = res[0];
                    if (!strTid.equals("0000000000000000") && !strTid.equals("000000000000000000000000")) {
                        strResult = "TID:" + strTid + "\n";
                    } else {
                        strResult = "";
                    }
                    Message msg = handler.obtainMessage();
                    msg.obj = strResult + "EPC:" + mContext.mReader.convertUiiToEPC(res[1]) + "@" + res[2];
                    handler.sendMessage(msg);
                }
            }
        }
    }

    @Override
    public void myOnKeyDwon() {
        readTag();
    }

}
