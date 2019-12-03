package com.egreen.egreenbeta05;

import android.content.pm.PackageInfo;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created by JDirectory on 2018. 11. 1..
 */

public class GetMarketVersion {
    public String getMarketVersion(String packageName) {
        String marketVersion = null;

        Log.i("패키지명: ", packageName);

        try {
            Document doc = Jsoup.connect("https://play.google.com/store/apps/details?id=" + packageName).get();
            Elements version = doc.select(".htlgb");

            for (int i=0; i<7; i++) {
                marketVersion = version.get(i).text();

                if (Pattern.matches("^[0-9]{1}.[0-9]{1}.[0-9]{1}$", marketVersion)) {
                    break;
                }
            }

//            for (Element mElement : version) {
//                return mElement.text().trim();
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return marketVersion;
    }
}
