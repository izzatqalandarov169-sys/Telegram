package org.telegram.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.LayoutHelper;

public class AdminPanelActivity extends BaseFragment {
    private LinearLayout root;

    @Override
    public View createView(Context context) {
        actionBar.setTitle("Admin panel");
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override public void onItemClick(int id) { if (id == -1) finishFragment(); }
        });

        ScrollView scroll = new ScrollView(context);
        root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(8), AndroidUtilities.dp(12), AndroidUtilities.dp(24));
        scroll.addView(root);

        addHeader("Boshqaruv");
        addButton("Stars qo'shish", v -> askUserAndNumber("Stars qo'shish", "Foydalanuvchi ID va Stars", "add_stars"));
        addButton("Stars ayirish", v -> askUserAndNumber("Stars ayirish", "Foydalanuvchi ID va Stars", "remove_stars"));
        addButton("Premium berish", v -> askUserAndNumber("Premium berish", "Foydalanuvchi ID va kun", "give_premium"));
        addButton("Premium narxini o'zgartirish", v -> askNumber("Yangi Premium narxi", "premium_price"));
        addButton("Stars narxini o'zgartirish", v -> askNumber("Yangi Stars narxi", "stars_price"));

        addHeader("Giftlar");
        addButton("Gift qo'shish", v -> presentFragment(new CustomGiftCreatorActivity()));
        addButton("Gift IDlarini ko'rish", v -> loadGiftIds());
        addButton("Gift kanalini ochish", v -> openGiftChannel());
        addButton("Giftlar oynasini ochish", v -> presentFragment(new CustomGiftStoreActivity()));

        addHeader("Foydalanuvchi boshqaruvi");
        addButton("Ban", v -> askUserOnly("Ban", "ban"));
        addButton("Unban", v -> askUserOnly("Unban", "unban"));
        addButton("Spam", v -> askUserOnly("Spam", "spam"));
        addButton("Unspam", v -> askUserOnly("Unspam", "unspam"));
        addButton("Mute", v -> askUserOnly("Mute", "mute"));
        addButton("Unmute", v -> askUserOnly("Unmute", "unmute"));

        TextView note = new TextView(context);
        note.setText("Gift yaratish, xarid va Stars/Premium boshqaruvi backend service orqali bajariladi.");
        note.setTextSize(13);
        note.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(14), AndroidUtilities.dp(8), 0);
        root.addView(note, LayoutHelper.createLinear(-1, -2));

        fragmentView = scroll;
        return scroll;
    }

    private void addHeader(String text) {
        TextView h = new TextView(getParentActivity());
        h.setText(text);
        h.setTextSize(14);
        h.setTypeface(null, android.graphics.Typeface.BOLD);
        h.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(7));
        root.addView(h, LayoutHelper.createLinear(-1, -2));
    }

    private void addButton(String text, View.OnClickListener listener) {
        Button b = new Button(getParentActivity());
        b.setText(text);
        b.setAllCaps(false);
        b.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        b.setOnClickListener(listener);
        root.addView(b, LayoutHelper.createLinear(-1, AndroidUtilities.dp(52), 0, 0, 0, 2));
    }

    private void askUserOnly(String title, String action) {
        final EditText e = field("Foydalanuvchi ID");
        new AlertDialog.Builder(getParentActivity()).setTitle(title).setView(e)
                .setPositiveButton("Bajarish", (d, w) -> {
                    try {
                        org.json.JSONObject body = new org.json.JSONObject();
                        body.put("user_id", Long.parseLong(e.getText().toString().trim()));
                        action(action, body);
                    } catch (Exception ignored) { Toast.makeText(getParentActivity(), "ID noto'g'ri", Toast.LENGTH_SHORT).show(); }
                }).setNegativeButton("Bekor qilish", null).show();
    }

    private void askUserAndNumber(String title, String hint, String action) {
        LinearLayout box = new LinearLayout(getParentActivity());
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(AndroidUtilities.dp(20), 0, AndroidUtilities.dp(20), 0);
        EditText user = field("Foydalanuvchi ID");
        EditText amount = field(hint);
        box.addView(user); box.addView(amount);
        new AlertDialog.Builder(getParentActivity()).setTitle(title).setView(box)
                .setPositiveButton("Bajarish", (d, w) -> {
                    try {
                        org.json.JSONObject body = new org.json.JSONObject();
                        body.put("user_id", Long.parseLong(user.getText().toString().trim()));
                        body.put("amount", Long.parseLong(amount.getText().toString().trim()));
                        action(action, body);
                    } catch (Exception e) { Toast.makeText(getParentActivity(), "Qiymatlarni tekshiring", Toast.LENGTH_SHORT).show(); }
                }).setNegativeButton("Bekor qilish", null).show();
    }

    private void askNumber(String title, String action) {
        EditText e = field("Qiymat");
        new AlertDialog.Builder(getParentActivity()).setTitle(title).setView(e)
                .setPositiveButton("Saqlash", (d, w) -> {
                    try {
                        org.json.JSONObject body = new org.json.JSONObject();
                        body.put("value", Long.parseLong(e.getText().toString().trim()));
                        action(action, body);
                    } catch (Exception ex) { Toast.makeText(getParentActivity(), "Qiymat noto'g'ri", Toast.LENGTH_SHORT).show(); }
                }).setNegativeButton("Bekor qilish", null).show();
    }

    private EditText field(String hint) {
        EditText e = new EditText(getParentActivity());
        e.setHint(hint);
        e.setInputType(InputType.TYPE_CLASS_TEXT);
        e.setSingleLine(true);
        return e;
    }

    private void action(String name, org.json.JSONObject body) {
        new Thread(() -> {
            try {
                body.put("action", name);
                body.put("admin_id", UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId());
                CustomGiftApi.postJson("/api/admin/action", body.toString());
                AndroidUtilities.runOnUIThread(() -> Toast.makeText(getParentActivity(), "So'rov yuborildi", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                FileLog.e(e);
                AndroidUtilities.runOnUIThread(() -> Toast.makeText(getParentActivity(), "Backend endpointini tekshiring", Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void openGiftChannel() {
        new Thread(() -> {
            try {
                String result = CustomGiftApi.get("/api/gifts/channel");
                AndroidUtilities.runOnUIThread(() -> {
                    try {
                        String url = new org.json.JSONObject(result).optString("url", "");
                        if (!url.isEmpty()) getParentActivity().startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)));
                        else Toast.makeText(getParentActivity(), "Gift kanali sozlanmagan", Toast.LENGTH_LONG).show();
                    } catch (Exception e) { Toast.makeText(getParentActivity(), "Kanal ma'lumoti noto'g'ri", Toast.LENGTH_SHORT).show(); }
                });
            } catch (Exception e) {
                FileLog.e(e);
                AndroidUtilities.runOnUIThread(() -> Toast.makeText(getParentActivity(), "Gift kanalini olishda xato", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void loadGiftIds() {
        new Thread(() -> {
            try {
                String result = CustomGiftApi.get("/api/admin/gifts");
                AndroidUtilities.runOnUIThread(() -> new AlertDialog.Builder(getParentActivity()).setTitle("Gift IDlari").setMessage(result).setPositiveButton("OK", null).show());
            } catch (Exception e) {
                FileLog.e(e);
                AndroidUtilities.runOnUIThread(() -> Toast.makeText(getParentActivity(), "Gift IDlarini olishda xato", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
