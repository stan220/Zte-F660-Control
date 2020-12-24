package com.stan220.ztef660control;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String tvMac = "00:51:ed:87:c3:0e";
    public static final String tabMac = "50:50:a4:89:6c:ca";
    public static final String alisaMac = "cc:4b:73:58:d7:8c";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView textView = findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());

        System.setOut(new PrintStream(new OutputStream() {

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            @Override public void write(int oneByte) {
                outputStream.write(oneByte);

                textView.setText(new String(outputStream.toByteArray()));
            }
        }));

        System.out.println("App started!!!");
    }

    @Override
    protected void onResume() {
        super.onResume();

        System.out.println("App resumed!!!");
        new DownloadFilesTask(this).execute();
    }

    public void onSwitchClick(View view) {
        System.out.println("### click: " + view);
        new DownloadFilesTask(this).execute((SwitchMaterial) view);
    }

    private static class DownloadFilesTask extends AsyncTask<SwitchMaterial, Void, List<String>> {

        private MainActivity context;

        public DownloadFilesTask(MainActivity context) {
            this.context = context;
        }

        @Override
        protected List<String> doInBackground(SwitchMaterial... switches) {
            List<String> result = new ArrayList<>();

            Socket s = null;
            try {
                s = new Socket(InetAddress.getByName("192.168.100.1"), 23);


                if (s.isConnected()) {
                    System.out.println("Connected!!!");
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter printWriter = new PrintWriter(s.getOutputStream(), true);

                in.readLine();
                in.readLine();
                printWriter.println("root");
                in.readLine();
                printWriter.println("Zte521");
                in.readLine();

                System.out.println(in.readLine());
                System.out.println(in.readLine());
                System.out.println(in.readLine());
                System.out.println(in.readLine());

                if (switches.length == 1) {
                    String mac = "00:00:00:00:00:00";
                    String cmd;
                    if (switches[0].getId() == R.id.switchTab) {
                        mac = tabMac;
                    }
                    if (switches[0].getId() == R.id.switchAlisa) {
                        mac = alisaMac;
                    }
                    if (switches[0].getId() == R.id.switchTv) {
                        mac = tvMac;
                    }

                    if (!switches[0].isChecked()) {
                        cmd = String.format("iptables -A macfilter -m mac --mac-source %s -j DROP\r\n", mac);
                    } else {
                        cmd = String.format("iptables -D macfilter -m mac --mac-source %s -j DROP\r\n", mac);
                    }

                    System.out.println(cmd);
                    printWriter.println(cmd);
                    System.out.println(in.readLine());
                    in.readLine();
                }

                printWriter.println("iptables -S | grep A\\ macfilter");
                in.readLine();
                printWriter.println();

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    printWriter.println();
                    if (inputLine.contains("#")) break;
                    if (inputLine.isEmpty()) continue;
                    System.out.println(inputLine);
                    result.add(inputLine.toLowerCase());
                }

                printWriter.println("exit");
                System.out.println(in.readLine());

                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("finish!");

            return result;
        }

        protected void onPostExecute(List<String> switchers) {
            SwitchMaterial switchTab = context.findViewById(R.id.switchTab);
            SwitchMaterial switchTv = context.findViewById(R.id.switchTv);
            SwitchMaterial switchAlisa = context.findViewById(R.id.switchAlisa);

            switchTab.setChecked(true);
            switchTv.setChecked(true);
            switchAlisa.setChecked(true);

            if (switchers == null) {
                return;
            }

            if (switchers.stream().anyMatch(str -> str.trim().toLowerCase().contains(tabMac))) {
                switchTab.setChecked(false);
            }
            if (switchers.stream().anyMatch(str -> str.trim().toLowerCase().contains(tvMac))) {
                switchTv.setChecked(false);
            }
            if (switchers.stream().anyMatch(str -> str.trim().toLowerCase().contains(alisaMac))) {
                switchAlisa.setChecked(false);
            }

            Toast.makeText(context, "Статус обновлён!", Toast.LENGTH_LONG).show();
        }

    }
}