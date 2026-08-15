package org.telegram.ui;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.LayoutHelper;

public class CustomGiftCreatorActivity extends BaseFragment {
    private static final int PICK_VIDEO = 4012;
    private Uri selectedVideo;
    private VideoView preview;
    private EditText titleField;
    private EditText starsField;
    private TextView status;

    @Override
    public View createView(Context context) {
        actionBar.setTitle("Gift qo'shish");
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override public void onItemClick(int id) { if (id == -1) finishFragment(); }
        });

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(16));
        root.setGravity(Gravity.TOP);

        TextView info = new TextView(context);
        info.setText("20 sekundgacha video tanlang. Video giftning asosiy animatsiyasi sifatida ishlatiladi.");
        info.setTextSize(15);
        root.addView(info, LayoutHelper.createLinear(-1, -2, 0, 0, 0, 12));

        titleField = new EditText(context);
        titleField.setHint("Gift nomi");
        root.addView(titleField, LayoutHelper.createLinear(-1, -2, 0, 0, 0, 8));

        starsField = new EditText(context);
        starsField.setHint("Stars narxini belgilang");
        starsField.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        root.addView(starsField, LayoutHelper.createLinear(-1, -2, 0, 0, 0, 8));

        Button pick = new Button(context);
        pick.setText("20 sekundgacha video tanlash");
        root.addView(pick, LayoutHelper.createLinear(-1, -2, 0, 0, 0, 8));

        preview = new VideoView(context);
        preview.setVisibility(View.GONE);
        root.addView(preview, LayoutHelper.createLinear(-1, AndroidUtilities.dp(220), 0, 0, 0, 8));

        Button save = new Button(context);
        save.setText("Giftni yaratish");
        root.addView(save, LayoutHelper.createLinear(-1, -2, 0, 0, 0, 8));

        Button store = new Button(context);
        store.setText("Giftlar do'koni");
        root.addView(store, LayoutHelper.createLinear(-1, -2, 0, 0, 0, 8));

        status = new TextView(context);
        root.addView(status, LayoutHelper.createLinear(-1, -2));

        pick.setOnClickListener(v -> chooseVideo());
        save.setOnClickListener(v -> upload());
        store.setOnClickListener(v -> presentFragment(new CustomGiftStoreActivity()));

        fragmentView = root;
        return root;
    }

    private void chooseVideo() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("video/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i, PICK_VIDEO);
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (requestCode != PICK_VIDEO || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            MediaMetadataRetriever r = new MediaMetadataRetriever();
            r.setDataSource(getParentActivity(), uri);
            String duration = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            r.release();
            long ms = duration == null ? 0 : Long.parseLong(duration);
            if (ms > 20000) {
                Toast.makeText(getParentActivity(), "Video 20 sekunddan uzun bo'lmasin", Toast.LENGTH_LONG).show();
                return;
            }
            selectedVideo = uri;
            preview.setVisibility(View.VISIBLE);
            preview.setVideoURI(uri);
            preview.start();
            status.setText("Video tayyor.");
        } catch (Exception e) {
            FileLog.e(e);
            Toast.makeText(getParentActivity(), "Video tekshirilmadi", Toast.LENGTH_SHORT).show();
        }
    }

    private void upload() {
        if (selectedVideo == null) {
            Toast.makeText(getParentActivity(), "Avval video tanlang", Toast.LENGTH_SHORT).show();
            return;
        }
        String title = titleField.getText().toString().trim();
        if (title.isEmpty()) title = "Custom Gift";
        long stars;
        try {
            stars = Long.parseLong(starsField.getText().toString().trim());
            if (stars <= 0) throw new Exception();
        } catch (Exception e) {
            Toast.makeText(getParentActivity(), "Stars narxini kiriting", Toast.LENGTH_SHORT).show();
            return;
        }

        final String finalTitle = title;
        status.setText("Gift yuklanmoqda...");
        new Thread(() -> {
            try {
                String result = CustomGiftApi.uploadVideo(getParentActivity(), selectedVideo, finalTitle, stars);
                AndroidUtilities.runOnUIThread(() -> {
                    if (result != null && result.contains("channel_required")) {
                        try {
                            org.json.JSONObject o = new org.json.JSONObject(result);
                            String channel = o.optString("channel", "");
                            if (!channel.isEmpty()) getParentActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(channel)));
                        } catch (Exception ignored) {}
                        status.setText("Avval gift kanaliga obuna bo'ling.");
                        Toast.makeText(getParentActivity(), "Gift yaratish uchun kanalga obuna bo'lish kerak", Toast.LENGTH_LONG).show();
                    } else {
                        status.setText("Gift yaratildi.");
                        Toast.makeText(getParentActivity(), "Gift muvaffaqiyatli yuborildi", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                FileLog.e(e);
                AndroidUtilities.runOnUIThread(() -> {
                    status.setText("Serverga yuborishda xato.");
                    Toast.makeText(getParentActivity(), "Backend /api/gifts endpointini tekshiring", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
