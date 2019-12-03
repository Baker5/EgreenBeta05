package com.egreen.egreenbeta05;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkStateCheck {
    Activity activity;

    public NetworkStateCheck(Activity activity) {
        this.activity = activity;
    }

    public boolean isConnectionNet() {
        boolean isConnection = false;

        ConnectivityManager connManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connManager != null;

        final String activityName = activity.getLocalClassName();

        if (connManager.getActiveNetworkInfo() != null && connManager.getActiveNetworkInfo().isConnected()) {
            //인터넷에 연결된 상태
            isConnection = true;
        }
        else {
            //인터넷 연결이 안되어 있는 상태
            AlertDialog.Builder ab = new AlertDialog.Builder(activity);
            ab.setMessage("인터넷 연결이 끊겼습니다.\n네트워크 상태를 확인하신 후, 다시 시도해주세요!");

            if (activityName.equals("A1_Main")) {
                ab.setPositiveButton("알겠어요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                    Log.i("NetCheck", activity.getLocalClassName());
                        activity.finish();
                    }
                });
            }
            else if (activityName.equals("A8_Learning")) {
                ab.setMessage("인터넷 연결이 끊겼습니다.\n출석체크가 안될 수도 있으니 꼭 확인해주세요.");
                ab.setPositiveButton("알겠어요", null);
            }
            else {
                ab.setPositiveButton("알겠어요",  null);
            }

            ab.show();
        }

        return isConnection;
    }
}
