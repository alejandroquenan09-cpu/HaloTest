package com.example.halotest;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String CHANNEL = "halo_test";
    private static final long DELAY_MS = 5000;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private NotificationManager nm;
    private TextView log;
    private boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationChannel ch = new NotificationChannel(
                CHANNEL, "Prueba Halo", NotificationManager.IMPORTANCE_HIGH);
        ch.setSound(null, null);
        ch.enableVibration(false);
        nm.createNotificationChannel(ch);

        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad * 2, pad, pad);

        TextView title = new TextView(this);
        title.setText("Prueba del aro Halo");
        title.setTextSize(22);
        root.addView(title);

        TextView help = new TextView(this);
        help.setText("1) Da permiso de notificaciones.\n"
                + "2) En Ajustes > Halo, activa el aro para esta app (Prueba Halo).\n"
                + "3) Toca una prueba, apaga la pantalla o pon el teléfono boca abajo "
                + "y mira el aro. Empieza a los 5 segundos.");
        help.setPadding(0, pad / 2, 0, pad);
        root.addView(help);

        addBtn(root, "Dar permiso de notificaciones", v -> askPermission());
        addBtn(root, "Abrir ajustes de notificaciones de esta app", v -> openSettings());
        addBtn(root, "Prueba 1: una sola notificación", v -> runTest(1, 1000));
        addBtn(root, "Prueba 2: 10 notificaciones, 1 por segundo", v -> runTest(10, 1000));
        addBtn(root, "Prueba 3: 20 notificaciones, cada 300 ms", v -> runTest(20, 300));
        addBtn(root, "Prueba 4: 40 notificaciones, cada 100 ms", v -> runTest(40, 100));
        addBtn(root, "Parar y limpiar", v -> stopAll());

        log = new TextView(this);
        log.setPadding(0, pad, 0, 0);
        root.addView(log);

        ScrollView sv = new ScrollView(this);
        sv.addView(root);
        setContentView(sv);
    }

    private void addBtn(LinearLayout parent, String text, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setOnClickListener(l);
        parent.addView(b);
    }

    private void say(String s) {
        log.append(s + "\n");
    }

    private void askPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        } else {
            say("Este Android no necesita pedir permiso.");
        }
    }

    private void openSettings() {
        Intent i = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        i.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        startActivity(i);
    }

    private void stopAll() {
        running = false;
        handler.removeCallbacksAndMessages(null);
        nm.cancelAll();
        say("Detenido.");
    }

    private void runTest(final int count, final long intervalMs) {
        if (running) {
            say("Ya hay una prueba corriendo.");
            return;
        }
        running = true;
        say("Empieza en 5 s: " + count + " notificaciones cada " + intervalMs + " ms");
        handler.postDelayed(new Runnable() {
            int i = 0;

            @Override
            public void run() {
                if (!running) return;
                if (i < count) {
                    send(i);
                    i++;
                    handler.postDelayed(this, intervalMs);
                } else {
                    handler.postDelayed(() -> {
                        nm.cancelAll();
                        running = false;
                        say("Terminó.");
                    }, 1500);
                }
            }
        }, DELAY_MS);
    }

    private void send(int n) {
        Notification.Builder b = new Notification.Builder(this, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Prueba Halo #" + (n + 1))
                .setContentText("Mira si el aro se enciende")
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setAutoCancel(true);
        nm.notify(1000 + n, b.build());
    }
}
