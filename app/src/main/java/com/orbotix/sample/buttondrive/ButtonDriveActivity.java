package com.orbotix.sample.buttondrive;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import orbotix.robot.base.DirectControlStrategy;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.internal.DeviceConnection;
import orbotix.robot.sensor.AttitudeSensor;
import orbotix.robot.sensor.DeviceSensorsData;
import orbotix.sphero.ConnectionListener;
import orbotix.sphero.DiscoveryListener;
import orbotix.sphero.NonPersistentOptionFlags;
import orbotix.sphero.PersistentOptionFlags;
import orbotix.sphero.SensorFlag;
import orbotix.sphero.SensorListener;
import orbotix.sphero.Sphero;
import orbotix.view.calibration.CalibrationCircle;

public class ButtonDriveActivity extends Activity {
    private ListView lstView;

    private List<Sphero> spheros;
    private Sphero mRobotMaster, mRobotSlave;
    private int spheroPosition = 0;

    double pitch, roll, yaw;
    final static int THRESHOLD = 11;

    private final SensorListener mSensorListener = new SensorListener() {
        @Override
        public void sensorUpdated(DeviceSensorsData datum) {
            //Show attitude data
            mRobotMaster.enableStabilization(false);
            mRobotMaster.setColor(255, 0, 0);
            mRobotMaster.setBackLEDBrightness(1f);

            mRobotSlave.enableStabilization(true);
            mRobotSlave.setColor(0, 255, 0);
            mRobotSlave.setBackLEDBrightness(1f);

            AttitudeSensor attitude = datum.getAttitudeData();
            if (attitude != null) {
                pitch = attitude.pitch;
                roll = attitude.roll;
                yaw = attitude.yaw;
            }

            driveSlave();

            //Log.d("DEBUG", "pitch = " + pitch + "; roll = " + roll + "; yaw = " + yaw);
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        spheros = new ArrayList<Sphero>();
        lstView = (ListView) findViewById(R.id.listView);

        lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //toast("Click ListItem Number: " + position);
                spheroPosition = position;
            }
        });

        RobotProvider.getDefaultProvider().addDiscoveryListener(new DiscoveryListener() {
            @Override
            public void onBluetoothDisabled() {
                toast("ERROR: CONST_VAR TEJAS NOT CONFIGURED; BLUETOOTH EXCEPTAMATATION ERROR");
            }

            @Override
            public void discoveryComplete(List<Sphero> robots) {
                updateListView();
                //toast("we has PINGASED it!");
                //mRobotSlave.setColor(255, 0, 0);
                //mRobotMaster.setColor(0, 255, 0);
            }

            @Override
            public void onFound(List<Sphero> robots) {
                //toast(RobotProvider.getDefaultProvider().getRobots().size());
                Sphero s = robots.iterator().next();
                if (!spheros.contains(s))
                    spheros.add(s);

                for (Sphero sp : RobotProvider.getDefaultProvider().getRobots())
                    if (!spheros.contains(sp))
                        spheros.add(sp);

                updateListView();
                if (spheros.size() >= 7) {
                    RobotProvider.getDefaultProvider().endDiscovery();
                }
            }
        });

        RobotProvider.getDefaultProvider().addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected(Robot robot) {
                toast("Connected " + (Sphero) robot);
                spamConnect((Sphero) robot);
            }

            @Override
            public void onConnectionFailed(Robot robot) {
                //toast("fck off " + (Sphero) robot);

                spamConnect((Sphero) robot);
            }

            @Override
            public void onDisconnected(Robot robot) {
                spamConnect((Sphero)robot);

                //toast("y u do dis " + (Sphero)robot);
            }
        });

        RobotProvider.getDefaultProvider().startDiscovery(getApplicationContext());
    }

    public void driveSlave() {
        pitch = (Math.abs(pitch) < THRESHOLD ? 0 : pitch);
        roll = (Math.abs(roll) < THRESHOLD ? 0 : roll);

        float heading = 0;

        //if (pitch == 0)
        //    heading = (float) Math.atan(roll/-pitch);

        if(pitch < -THRESHOLD) { mRobotSlave.drive(0f, 0.5f); }
        else if(pitch > THRESHOLD) { mRobotSlave.drive(180f, 0.5f); }
        else { mRobotSlave.stop(); }

        if(roll > THRESHOLD) { mRobotSlave.drive(90f, 0.5f); }
        else if(roll < -THRESHOLD) { mRobotSlave.drive(270, 0.5f); }
        else { mRobotSlave.stop(); }
    }

    public boolean startServer(View v) {
        if (mRobotMaster == null || mRobotSlave == null) {
            toast("found nool");
            return false;
        } else if (!mRobotMaster.isConnected() || !mRobotSlave.isConnected()) {
            toast("master = " + mRobotMaster.isConnected());
            toast("slave = " + mRobotSlave.isConnected());
            return false;
        } else {
            toast("Starting server...");
            requestDataStreaming();

            /*mRobotMaster.setControlStrategy(new DirectControlStrategy(new DeviceConnection(mRobotMaster)));
            mRobotMaster.setControlStrategy(new ControlStrategy() {
                @Override
                public void doCommand(DeviceCommand deviceCommand, long l) {
                    mRobotMaster.enableStabilization(false);
                    mRobotMaster.setColor(255, 255, 0);
                    mRobotMaster.setBackLEDBrightness(100);
                    mRobotSlave.setControlStrategy(new DirectControlStrategy(new DeviceConnection(mRobotSlave)));
                    mRobotSlave.enableStabilization(true);
                    mRobotSlave.setColor(0, 255, 255);
                    mRobotSlave.setBackLEDBrightness(100);requestDataStreaming();
                }

                @Override
                public void close() throws IOException {
                    toast("I have no idea.");

                }
            });*/
            /*
            mRobotMaster.enableStabilization(false);
            mRobotMaster.setColor(255, 255, 0);
            mRobotMaster.setBackLEDBrightness(100);
            mRobotSlave.setControlStrategy(new DirectControlStrategy(new DeviceConnection(mRobotSlave)));
            mRobotSlave.enableStabilization(true);
            mRobotSlave.setColor(0, 255, 255);
            mRobotSlave.setBackLEDBrightness(100);

            requestDataStreaming();
            return true;*/
            return true;
        }
    }

    private void requestDataStreaming() {
        if(mRobotMaster != null) {
            mRobotMaster.getSensorControl().setRate(20 /*Hz*/);
            mRobotMaster.getSensorControl().addSensorListener(mSensorListener, SensorFlag.ATTITUDE);
        }
    }

    public void spamConnect(Sphero sphero) {
        for (int i : new int[5])
            RobotProvider.getDefaultProvider().connect(sphero);
    }

    public void selectSphero(View v) {
        String mode = (String) v.getTag();
        toast("spheroPosition = " + spheroPosition);
        if (mode.equals("master")) {
            mRobotMaster = spheros.get(spheroPosition);
            spamConnect(mRobotMaster);

            mRobotMaster.startCalibration();

            mRobotMaster.setControlStrategy(new DirectControlStrategy(new DeviceConnection(mRobotMaster)));
            mRobotMaster.enableStabilization(false);
            mRobotMaster.setColor(255, 0, 0);

            Log.d("DEBUG", "setting color to red :)");
            //blinkBackLight(mRobotMaster, 30);

            mRobotMaster.stopCalibration(true);

            spheros.remove(spheroPosition);
            updateListView();
        } else if (mode.equals("slave")) {
            mRobotSlave = spheros.get(spheroPosition);
            spamConnect(mRobotSlave);

            mRobotSlave.startCalibration();

            if (mRobotMaster != null) {
                mRobotSlave.getConfiguration().setHeading(0f);

                /*try{
                    for (int x = 0; x < 5000; x += 100) {
                        mRobotSlave.setBackLEDBrightness(1f);
                        Thread.sleep(100);
                        mRobotSlave.setBackLEDBrightness(0f);
                    }
                } catch(Exception ex) {}*/

                mRobotSlave.getConfiguration().update();
            }

            mRobotSlave.setControlStrategy(new DirectControlStrategy(new DeviceConnection(mRobotSlave)));
            mRobotSlave.enableStabilization(true);
            mRobotSlave.setColor(0, 255, 0);

            //blinkBackLight(mRobotSlave, 20);

            mRobotSlave.stopCalibration(true);
            spheros.remove(spheroPosition);
            updateListView();
        }
    }

    public void updateListView() {
        lstView.clearChoices();
        List<String> names = new ArrayList<String>(spheros.size());

        for (int i = 0; i < spheros.size(); i++) {
            String tmp = spheros.get(i).getName();
            names.add(tmp);
        }

        final ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, R.layout.row_text_view, names);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lstView.setAdapter(listAdapter);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        RobotProvider.getDefaultProvider().startDiscovery(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        RobotProvider.getDefaultProvider().disconnectControlledRobots();
    }

    public void toast(Object obj) {
        Toast.makeText(getApplicationContext(), obj.toString(), Toast.LENGTH_SHORT).show();
        Log.d("DEBUG", obj.toString());
    }
}