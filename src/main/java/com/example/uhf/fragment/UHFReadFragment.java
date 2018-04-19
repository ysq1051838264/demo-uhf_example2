package com.example.uhf.fragment;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.example.uhf.R;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.tools.StringUtils;
import com.example.uhf.tools.UIHelper;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.rscja.deviceapi.RFIDWithUHF.BankEnum;
import com.rscja.deviceapi.entity.SimpleRFIDEntity;

public class UHFReadFragment extends KeyDwonFragment implements OnClickListener{

    private UHFMainActivity mContext;

    Spinner SpinnerBank_Read;
    EditText EtPtr_Read;
    EditText EtLen_Read;
    EditText EtAccessPwd_Read;
    Spinner SpinnerOption_Read;
    EditText EtPtr2_Read;
    EditText EtLen2_Read;
    EditText EtData_Read;
    Button BtRead;

    CheckBox cb_filter;
    EditText etPtr_filter;
    EditText etData_filter;
    EditText etLen_filter;
    RadioButton rbEPC_filter;
    RadioButton rbTID_filter;
    RadioButton rbUser_filter;
    CheckBox cb_QT_R;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uhf_read_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = (UHFMainActivity) getActivity();
        SpinnerBank_Read = (Spinner) getView().findViewById(R.id.SpinnerBank_Read);
        EtPtr_Read = (EditText) getView().findViewById(R.id.EtPtr_Read);
        EtLen_Read = (EditText) getView().findViewById(R.id.EtLen_Read);
        EtAccessPwd_Read = (EditText) getView().findViewById(R.id.EtAccessPwd_Read);
        SpinnerOption_Read = (Spinner) getView().findViewById(R.id.SpinnerOption_Read);
        EtPtr2_Read = (EditText) getView().findViewById(R.id.EtPtr2_Read);
        EtLen2_Read = (EditText) getView().findViewById(R.id.EtLen2_Read);
        EtData_Read = (EditText) getView().findViewById(R.id.EtData_Read);
        etLen_filter=(EditText)getView().findViewById(R.id.etLen_filter);
        BtRead = (Button) getView().findViewById(R.id.BtRead);

        cb_QT_R = (CheckBox) getView().findViewById(R.id.cb_QT_R);
        cb_filter = (CheckBox) getView().findViewById(R.id.cb_filter);
        etPtr_filter = (EditText) getView().findViewById(R.id.etPtr_filter);
        etData_filter = (EditText) getView().findViewById(R.id.etData_filter);
        rbEPC_filter = (RadioButton) getView().findViewById(R.id.rbEPC_filter);
        rbTID_filter = (RadioButton) getView().findViewById(R.id.rbTID_filter);
        rbUser_filter = (RadioButton) getView().findViewById(R.id.rbUser_filter);

        rbEPC_filter.setOnClickListener(this);
        rbTID_filter.setOnClickListener(this);
        rbUser_filter.setOnClickListener(this);
        BtRead.setOnClickListener(this);


        EtData_Read.setKeyListener(null);
        EtPtr2_Read.setEnabled(false);
        EtLen2_Read.setEnabled(false);
        EtData_Read.setText("");

        cb_filter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    String data = etData_filter.getText().toString().trim();
                    String rex = "[\\da-fA-F]*"; //匹配正则表达式，数据为十六进制格式
                    if(data==null || data.isEmpty() || !data.matches(rex)) {
                        UIHelper.ToastMessage(mContext,"过滤的数据必须是十六进制数据");
                        cb_filter.setChecked(false);
                        return;
                    }
                }
            }
        });
        SpinnerBank_Read.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String element = adapterView.getItemAtPosition(i).toString();// 得到spanner的值
                if(element.equals("EPC")){
                    EtPtr_Read.setText("2");
                }else{
                    EtPtr_Read.setText("0");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onClick(View view) {
          switch (view.getId()){
              case  R.id.rbEPC_filter:
                  if(rbEPC_filter.isChecked()){
                      etPtr_filter.setText("32");
                  }
                  break;
              case  R.id.rbTID_filter:
                  if(rbTID_filter.isChecked()){
                      etPtr_filter.setText("0");
                  }
                  break;
              case  R.id.rbUser_filter:
                  if(rbUser_filter.isChecked()){
                      etPtr_filter.setText("0");
                  }
                  break;
              case  R.id.BtRead:
                  read();
                  break;
          }

    }




    private void read() {
        String ptrStr = EtPtr_Read.getText().toString().trim();
        if (ptrStr.equals("")) {
            UIHelper.ToastMessage(mContext, R.string.uhf_msg_addr_not_null);
            return;
        } else if (!TextUtils.isDigitsOnly(ptrStr)) {
            UIHelper.ToastMessage(mContext, R.string.uhf_msg_addr_must_decimal);
            return;
        }

        String cntStr = EtLen_Read.getText().toString().trim();
        if (cntStr.equals("")) {
            UIHelper.ToastMessage(mContext, R.string.uhf_msg_len_not_null);
            return;
        } else if (!TextUtils.isDigitsOnly(cntStr)) {
            UIHelper.ToastMessage(mContext, R.string.uhf_msg_len_must_decimal);
            return;
        }

        String pwdStr = EtAccessPwd_Read.getText().toString().trim();
        if (!TextUtils.isEmpty(pwdStr)) {
            if (pwdStr.length() != 8) {
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_addr_must_len8);
                return;
            } else if (!mContext.vailHexInput(pwdStr)) {
                UIHelper.ToastMessage(mContext, R.string.rfid_mgs_error_nohex);
                return;
            }
        } else {
            pwdStr = "00000000";
        }

        boolean result=false;
        String Bank="";
        if(SpinnerBank_Read.getSelectedItemPosition()==1){
            Bank="UII";
        }else{
            Bank=SpinnerBank_Read.getSelectedItem().toString();
        }
        if (cb_filter.isChecked())//  过滤
        {
            if(etPtr_filter.getText().toString()==null || etPtr_filter.getText().toString().isEmpty()){
                UIHelper.ToastMessage(mContext, "过滤数据的起始地址不能为空");
                return;
            }
            if(etData_filter.getText().toString()==null || etData_filter.getText().toString().isEmpty()){
                UIHelper.ToastMessage(mContext, "过滤数据不能为空");
                return;
            }
            if(etLen_filter.getText().toString()==null || etLen_filter.getText().toString().isEmpty()){
                UIHelper.ToastMessage(mContext, "过滤数据长度不能为空");
                return;
            }


            int filterPtr=Integer.parseInt(etPtr_filter.getText().toString());
            String filterData=etData_filter.getText().toString();
            int filterCnt=Integer.parseInt(etLen_filter.getText().toString());
            String filterBank="UII";
            if(rbEPC_filter.isChecked()){
                filterBank="UII";
            }else if(rbTID_filter.isChecked()){
                filterBank="TID";
            }else if(rbUser_filter.isChecked()){
                filterBank="USER";
            }
            String data="";
            if(cb_QT_R.isChecked()) {
                data = mContext.mReader.readDataWithQT(pwdStr,
                        BankEnum.valueOf(filterBank),
                        filterPtr,
                        filterCnt,
                        filterData,
                        BankEnum.valueOf(Bank),
                        Integer.parseInt(ptrStr),
                        Integer.parseInt(cntStr)
                );
            }else{
                data = mContext.mReader.readData(pwdStr,
                        BankEnum.valueOf(filterBank),
                        filterPtr,
                        filterCnt,
                        filterData,
                        BankEnum.valueOf(Bank),
                        Integer.parseInt(ptrStr),
                        Integer.parseInt(cntStr)
                );
            }
            if(data!=null && data.length()>0){
                result=true;
            }else {
                result=false;
            }
            EtData_Read.setText("data：" +data);
        } else {
            SimpleRFIDEntity entity;
            if(cb_QT_R.isChecked()){
                entity= mContext.mReader.readDataWithQT(pwdStr,
                        BankEnum.valueOf(Bank),
                        Integer.parseInt(ptrStr),
                        Integer.parseInt(cntStr));
            }else {
                entity = mContext.mReader.readData(pwdStr,
                        BankEnum.valueOf(Bank),
                        Integer.parseInt(ptrStr),
                        Integer.parseInt(cntStr));
            }
            if (entity != null) {
                result = true;
                EtData_Read.setText("data：" + entity.getData());
            } else {
                result = false;
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_read_data_fail);
                EtData_Read.setText("");
            }
        }
        if(!result){
            mContext.playSound(2);
        }else{
            mContext.playSound(1);
        }
    }
    public void myOnKeyDwon() {
        read();
    }
}
