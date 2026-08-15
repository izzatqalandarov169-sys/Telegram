package org.telegram.ui;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.LayoutHelper;

public class CustomGiftStoreActivity extends BaseFragment {
    private LinearLayout list;

    @Override
    public View createView(Context context) {
        actionBar.setTitle("Giftlar");
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override public void onItemClick(int id) {
                if (id == -1) finishFragment();
            }
        });

        ScrollView scroll = new ScrollView(context);
        list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(24));
        scroll.addView(list);
        fragmentView = scroll;
        load();
        return scroll;
    }

    private void load() {
        TextView loading = new TextView(getParentActivity());
        loading.setText("Giftlar yuklanmoqda...");
        list.addView(loading);
        new Thread(() -> {
            try {
                String json = CustomGiftApi.get("/api/gifts");
                AndroidUtilities.runOnUIThread(() -> render(json));
            } catch (Exception e) {
                FileLog.e(e);
                AndroidUtilities.runOnUIThread(() -> {
                    list.removeAllViews();
                    TextView t = new TextView(getParentActivity());
                    t.setText("Giftlar yuklanmadi. Backend /api/gifts endpointini tekshiring.");
                    list.addView(t);
                });
            }
        }).start();
    }

    private void render(String json) {
        list.removeAllViews();
        TextView info = new TextView(getParentActivity());
        info.setText("Giftlar sizning kanal/service'ingiz orqali keladi.");
        info.setTextSize(15);
        list.addView(info, LayoutHelper.createLinear(-1, -2, 0, 0, 0, 12));
        try {
            org.json.JSONArray arr = new org.json.JSONArray(json);
            if (arr.length() == 0) {
                TextView empty = new TextView(getParentActivity());
                empty.setText("Hozircha gift yo'q.");
                list.addView(empty);
                return;
            }
            for (int i = 0; i < arr.length(); i++) addGift(arr.getJSONObject(i));
        } catch (Exception e) {
            TextView bad = new TextView(getParentActivity());
            bad.setText("Backend javobi noto'g'ri formatda.");
            list.addView(bad);
        }
    }

    private void addGift(org.json.JSONObject g) {
        LinearLayout card = new LinearLayout(getParentActivity());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(AndroidUtilities.dp(14), AndroidUtilities.dp(12), AndroidUtilities.dp(14), AndroidUtilities.dp(12));

        VideoView video = new VideoView(getParentActivity());
        String videoUrl = g.optString("video_url", "");
        if (!videoUrl.isEmpty()) {
            if (videoUrl.startsWith("/")) videoUrl = CustomGiftApi.BASE_URL + videoUrl;
            video.setVideoURI(Uri.parse(videoUrl));
            video.setVisibility(View.VISIBLE);
        } else video.setVisibility(View.GONE);
        card.addView(video, LayoutHelper.createLinear(-1, AndroidUtilities.dp(180), 0, 0, 0, 6));
        video.setOnClickListener(v -> { if (video.isPlaying()) video.pause(); else video.start(); });

        TextView title = new TextView(getParentActivity());
        title.setText(g.optString("title", "Gift"));
        title.setTextSize(17);
        card.addView(title);

        TextView price = new TextView(getParentActivity());
        price.setText(g.optLong("stars", 0) + " Stars");
        card.addView(price);

        Button buy = new Button(getParentActivity());
        buy.setText("Olish");
        card.addView(buy);
        String id = g.optString("id", "");
        buy.setOnClickListener(v -> buy(id));
        list.addView(card, LayoutHelper.createLinear(-1, -2, 0, 0, 0, 8));
    }

    private void buy(String id) {
        new Thread(() -> {
            try {
                org.json.JSONObject body = new org.json.JSONObject();
                body.put("gift_id", id);
                body.put("user_id", org.telegram.messenger.UserConfig.getInstance(org.telegram.messenger.UserConfig.selectedAccount).getClientUserId());
                String response = CustomGiftApi.postJson("/api/gifts/purchase", body.toString());
                AndroidUtilities.runOnUIThread(() -> {
                    try {
                        org.json.JSONObject o = new org.json.JSONObject(response);
                        if (o.optBoolean("ok")) Toast.makeText(getParentActivity(), "Gift olindi", Toast.LENGTH_SHORT).show();
                        else Toast.makeText(getParentActivity(), o.optString("error", "Giftni olishda xato"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getParentActivity(), "Giftni olishda xato", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                FileLog.e(e);
                AndroidUtilities.runOnUIThread(() -> Toast.makeText(getParentActivity(), "Giftni olishda xato", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
