//////////////////////////////////////////////////////////////////////////////////
//Nucleo Radar: realize a LIDAR using STM32 nucleo board and an android terminal
//
// Dev: Sylvain TRIVIAUX /
// copyright GILISYMO  2017
// www.gilisymo.com
/////////////////////////////////////////////////////////////////////////////////



package fr.free.triviaux.sylvain.nucleoradar;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address = "00:19:5D:EF:02:A2";

    //https://www.intorobotics.com/how-to-develop-simple-bluetooth-android-application-to-control-a-robot-remote/

//From http://stackoverflow.com/questions/13450406/how-to-receive-serial-data-using-android-bluetooth
    BluetoothDevice mmDevice;
    BluetoothSocket mmSocket;

    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;
    Canvas canvas;
    Bitmap bg;

    Boolean bRotate = Boolean.TRUE;
    //TextView t = (TextView)findViewById(R.id.textView);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView t = (TextView)findViewById(R.id.textView);
       Button button1 =  (Button) findViewById(R.id.button1);
        button1.setOnClickListener(onClickListener);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //Check if terminal offer Bluetooth capabiliies
        if (bluetoothAdapter == null)
            Toast.makeText(MainActivity.this, "Bluetooth is absent from terminal app will not run",
                    Toast.LENGTH_SHORT).show();


        //else
        //    Toast.makeText(MainActivity.this, "Bluetooth is present on terminal",
        //            Toast.LENGTH_SHORT).show();


        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else{
            checkBT();
        }


        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#000000"));
       // Bitmap bg = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888);
        bg = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bg);
        //canvas.drawRect(0, 0, 480, 800, paint);
        RelativeLayout ll = (RelativeLayout) findViewById(R.id.rect);
        ll.setBackgroundDrawable(new BitmapDrawable(bg));
        ///stDrawOnCanvas(canvas);

       // checkBT();

      /*  MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Log.d("UI thread", "I am the UI thread");
                stDrawOnCanvas(canvas);
            }
        });*/



    }


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            switch (v.getId()) {
                case R.id.button1: {
                    //Bitmap bg = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888);
                    //canvas = new Canvas(bg);
                    canvas.drawColor(Color.WHITE);
                                                        /*
                    /*
                    Paint paint = new Paint();
                    paint.setColor(Color.parseColor("#000000"));


                    //canvas.drawRect(0, 0, 480, 800, paint);
                    RelativeLayout ll = (RelativeLayout) findViewById(R.id.rect);
                    ll.setBackgroundDrawable(new BitmapDrawable(bg));
                    //stDrawOnCanvas(canvas);
                    */
                    break;
                }

                case R.id.button2: {

                    bRotate  = ! bRotate;
                    /*
                    /*
                    Paint paint = new Paint();
                    paint.setColor(Color.parseColor("#000000"));


                    //canvas.drawRect(0, 0, 480, 800, paint);
                    RelativeLayout ll = (RelativeLayout) findViewById(R.id.rect);
                    ll.setBackgroundDrawable(new BitmapDrawable(bg));
                    //stDrawOnCanvas(canvas);
                    */
                    break;
                }
            }
        }

    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode ==REQUEST_ENABLE_BT) {


            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(MainActivity.this, "Sylvain BT cancelled",  Toast.LENGTH_SHORT).show();
            }

            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "Sylvain BT OK",  Toast.LENGTH_SHORT).show();


            }

            checkBT();
        }
    }

    private void checkBT()
    {
        TextView t = (TextView)findViewById(R.id.textView);

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        showBTDialog();
        /*
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();

                if(deviceName.equals("WIDE_HK")) {
                    mmDevice = device;
                    Toast.makeText(MainActivity.this, "WIDE_HK FOUND",
                            Toast.LENGTH_SHORT).show();
                }

            String deviceHardwareAddress = device.getAddress(); // MAC address
            t.append("\n  Device: " + device.getName() + ", " + device);
            //t.setText(deviceName);
            }

            if(mmDevice == null)
                Toast.makeText(MainActivity.this, "mmDevice NULL1",
                        Toast.LENGTH_SHORT).show();
            try
            {

                openBT();
            }
            catch (IOException ex) {
                Log.d("NUCLEORADAR", ex.getMessage());
                Toast.makeText(MainActivity.this, ex.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }

        }
        else {
            Toast.makeText(MainActivity.this, "No device bound",
                    Toast.LENGTH_SHORT).show();
        }
        */


    }


    private void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        if(mmDevice == null)
            Toast.makeText(MainActivity.this, "mmDevice NULL",
                    Toast.LENGTH_SHORT).show();
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);

        mmSocket.connect();


        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

  //      showBTDialog();
        beginListenForData();
    }


    public void showBTDialog() {

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View Viewlayout = inflater.inflate(R.layout.popup_bt, (ViewGroup) findViewById(R.id.listView));

        popDialog.setTitle("Paired Bluetooth Devices");
        popDialog.setView(Viewlayout);

        // create the arrayAdapter that contains the BTDevices, and set it to a ListView
        final  ListView myListView = (ListView) Viewlayout.findViewById(R.id.listView);
        ArrayAdapter<String> BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        myListView.setAdapter(BTArrayAdapter);

         myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View view, int arg2, long itemID) {

                View itemView = view;
                int position = (int) arg0.getSelectedItemId();
               // Toast.makeText(getApplicationContext(), " Position:" + position + " arg2:" + arg2 + " arg3:" + itemID, Toast.LENGTH_LONG).show();
                String text = myListView.getItemAtPosition(arg2).toString().trim();
                String[] deviceData = text.split("\n");
                Toast.makeText(getApplicationContext(), deviceData[0], Toast.LENGTH_LONG).show();



                for (BluetoothDevice device :   bluetoothAdapter.getBondedDevices()) {
                    String deviceName = device.getName();
                    Toast.makeText(getApplicationContext(), deviceData[0], Toast.LENGTH_SHORT).show();
                    if(deviceName.equals(deviceData[0])) {
                        mmDevice = device;
                        Toast.makeText(MainActivity.this, text+" FOUND",
                                Toast.LENGTH_SHORT).show();
                    }
                }


                if(mmDevice == null)
                    Toast.makeText(MainActivity.this, "mmDevice NULL1",
                            Toast.LENGTH_SHORT).show();
                try
                {
                    openBT();
                }
                catch (IOException ex) {
                    Log.d("NUCLEORADAR", ex.getMessage());
                    Toast.makeText(MainActivity.this, ex.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
                }
             });

            // get paired devices
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            // put it's one to the adapter
            for(
            BluetoothDevice device
            :pairedDevices)
                    BTArrayAdapter.add(device.getName()+"\n"+device.getAddress());


            // Button OK
            popDialog.setPositiveButton("close",
                    new DialogInterface.OnClickListener()

            {
                public void onClick (DialogInterface dialog,int which){
                dialog.dismiss();
            }

            }

            );

            // Create popup and show
            popDialog.create();
            popDialog.show();

        }
1

    void beginListenForData()
    {


        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                final TextView t = (TextView)findViewById(R.id.textView);
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {

                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    //final TextView t = (TextView) findViewById(R.id.textView);
                                    //String tmp = data + "\n"
                                   // t.append(data);
                                    handler.post(new Runnable()
                                    {
                                        public void run() {
                                            // final TextView t = (TextView) findViewById(R.id.textView);
                                            //String tmp = data + "\n";
                                            t.setText(data);
                                            //data.trim();
                                            String[] separated = data.split(":");

                                            //Toast.makeText(MainActivity.this, separated[1],  Toast.LENGTH_SHORT).show();
                                            if ((separated[0].length() != 0)&& (separated[1].length() != 0)) {

                                            separated[1].trim();
                                            t.append("\n" + separated[0]);
                                            t.append("\n" + separated[1]);

                                            t.append("\ntest");
                                            String s_angle = separated[0];
                                            String s_value = separated[1];


                                             if(bRotate == Boolean.FALSE) {
                                                 s_angle = "90";
                                                 Toast.makeText(MainActivity.this, "Rotation stopped",  Toast.LENGTH_SHORT).show();
                                             }
                                            final Integer angle = Integer.parseInt(s_angle);

                                            //final Integer value = Integer.parseInt(s_value)/100;
                                            final Integer value = Integer.parseInt(s_value);

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        //paint.setColor(Color.parseColor("#6b8728"));
                                                        //Bitmap bg = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888);
                                                        //Canvas canvas = new Canvas(bg);
                                                        Integer width = canvas.getWidth() / 2;
                                                        Integer height = canvas.getHeight() / 2;

                                                        //canvas.drawLine(width, height, 150, 50, paint);
                                                        if((angle <= 5) || (angle >= 175))
                                                            canvas.drawColor(Color.WHITE);
                                                            //canvas.drawColor(Color.TRANSPARENT);
                                                        /*
                                                        int i;
                                                        for(i=0; i<=180; i++){
                                                            stDrawLineAngle(canvas.getWidth() / 2, canvas.getHeight() / 2, value, angle, "#ffffff", canvas);
                                                        }
                                                        */
                                                        //Resources res = getResources();
                                                        //Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.radar_960_720);
                                                        float toto = canvas.getWidth();
                                                        float tutu = toto/2;
                                                        float valuedraw =(value*canvas.getWidth())/5000;
                                                        if(valuedraw >canvas.getWidth()/2 ) {
                                                            valuedraw = (float) canvas.getWidth()/2;
                                                        }

                                                        t.append("\n" + valuedraw);
                                                        stDrawLineAngle(canvas.getWidth() / 2, canvas.getHeight() / 2, valuedraw, angle-1, "#fe201d", canvas);
                                                        stDrawLineAngle(canvas.getWidth() / 2, canvas.getHeight() / 2, valuedraw, angle, "#ff211e", canvas);
                                                        stDrawLineAngle(canvas.getWidth() / 2, canvas.getHeight() / 2, valuedraw, angle+1, "#fe201d", canvas);
                                                        //
                                                        //stDrawLineAngle(canvas.getWidth() / 2, canvas.getHeight() / 2, value, angle-5, "#ffffff", canvas);
                                                        //stDrawLineAngle(canvas.getWidth() / 2, canvas.getHeight() / 2, value, angle+5, "#ffffff", canvas);
                                                        //stDrawBeam(canvas.getWidth() / 2, canvas.getHeight() / 2, value, angle, "#ff211e", canvas);
                                                        /*
                                                        if (angle >= 5) {
                                                            //canvas.drawColor(Color.WHITE);
                                                          //  stDrawLineAngle(canvas.getWidth() / 2, canvas.getHeight() / 2, 40000, angle - 5, "#ffffff", canvas);
                                                            stDrawBeam(canvas.getWidth() / 2, canvas.getHeight() / 2, value, angle, "#ff211e", canvas);
                                                        }
                                                        */


                                                    }
                                                });





                                            //stDrawLineAngle(canvas.getWidth() / 2, canvas.getHeight() / 2, value, angle, "ff211e", canvas);
/*
                                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        //your UI code goes here
                                                        Toast.makeText(MainActivity.this, value,  Toast.LENGTH_SHORT).show();
                                                        //stDrawLineAngle(canvas.getWidth() / 2, canvas.getHeight() / 2, value, angle, "ff211e", canvas);
                                                    }
                                                });*/

                                         }
/*
                                            //stDrawLineAngle(  canvas.getWidth()/2,  canvas.getHeight()/2, value, angle, "ff211e", canvas);
                                            //http://www.color-hex.com/
                                            //#6b8728 = Green
                                            //#00ff6d = vert clair
                                            //#ff211e = rouge
*/
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                        else
                        {
                            //t.append(bytesAvailable+"\n");
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });
        workerThread.start();
    }


 //public void stDrawOnCanvas(int lineLength, int angle)
 public void stDrawOnCanvas(Canvas canvas)
 {
     Paint paint = new Paint();
     paint.setColor(Color.parseColor("#6b8728"));
     //Bitmap bg = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888);
     //Canvas canvas = new Canvas(bg);
     int width = canvas.getWidth()/2;
     int height = canvas.getHeight()/2;
     float radius = 240;
     /*


 for(double i=0; i<=180; i=i+0.1) {
     double irad = Math.toRadians(i);
     paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
     //canvas.drawLine(width, height, i, 50, paint);
     // canvas.drawLine(width, height, (float) (width + Math.cos(height / width)), (float) (height + Math.sin(height / width)), paint);
     paint.setXfermode(null);
     // canvas.drawLine(width, height, i, 50, paint);
     canvas.drawLine(width, height, (float) (width - (radius * Math.cos(irad))), (float) (height - (radius * Math.sin(irad))), paint);


     //canvas.drawLine(width,height,150,50, paint);

 }*/
     RelativeLayout ll = (RelativeLayout) findViewById(R.id.rect);
    //41 ll.setBackgroundDrawable(new BitmapDrawable(bg));
     //stDrawLineAngle(width, height, 500, 75, "ff211e", canvas, paint);
     //stDrawHalfCircle(width, height, radius, "#6b8728", canvas);


 }

    public void stDrawHalfCircle(int x, int y, float radius, String  color,  Canvas canvas) {

        Paint paint = new Paint();
        //#6b8728 = Green
        // paint.setColor(Color.parseColor("#6b8728"));
        paint.setColor(Color.parseColor(color));

        for (double i = 0; i <= 180; i = i + 0.1) {
            double irad = Math.toRadians(i);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            //canvas.drawLine(width, height, i, 50, paint);
            // canvas.drawLine(width, height, (float) (width + Math.cos(height / width)), (float) (height + Math.sin(height / width)), paint);
            paint.setXfermode(null);
            // canvas.drawLine(width, height, i, 50, paint);
            canvas.drawLine(x, y, (float) ( (radius * Math.cos(irad))), (float) ( (radius * Math.sin(irad))), paint);
        }
    }


    public void stDrawLineAngle(int x, int y, float radius, Integer angle,  String  color,  Canvas canvas) {
        //http://www.color-hex.com/
        //#6b8728 = Green
        //#00ff6d = vert clair
        //#ff211e = rouge
        RelativeLayout ll = (RelativeLayout) findViewById(R.id.rect);
        ll.setBackgroundDrawable(new BitmapDrawable(bg));

        Paint paint = new Paint();
        //canvas.drawLine(x, y, 150, 50, paint);
        paint.setColor( Color.parseColor(color));

         angle = 180-angle;
         double dang = (double) angle;


         double irad = Math.toRadians( dang);


        //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        //canvas.drawLine(width, height, i, 50, paint);
        // canvas.drawLine(width, height, (float) (width + Math.cos(height / width)), (float) (height + Math.sin(height / width)), paint);
        //paint.setXfermode(null);
        // canvas.drawLine(width, height, i, 50, paint);
        //
       canvas.drawLine( (float) (x), (float) (y), (float) (x - (radius * Math.cos(irad))), (float) (y - (radius * Math.sin(irad))), paint);

    }

    public void stDrawBeam(int x, int y, float radius, Integer angle,  String  color,  Canvas canvas) {

        stDrawLineAngle(canvas.getWidth() / 2, canvas.getHeight() / 2, (float)  (radius), (Integer) angle-1, "#336600", canvas);
        stDrawLineAngle(canvas.getWidth() / 2, canvas.getHeight() / 2, (float)  (radius), (Integer) angle-2, "#4D9900", canvas);
        stDrawLineAngle(canvas.getWidth() / 2, canvas.getHeight() / 2, (float)  (radius), (Integer) angle-3, "#66CC00", canvas);
        stDrawLineAngle(canvas.getWidth() / 2, canvas.getHeight() / 2, (float)  (radius), (Integer) angle-4, "#80FF00", canvas);
        stDrawLineAngle(canvas.getWidth() / 2, canvas.getHeight() / 2, (float)  (radius), (Integer) angle-5, "#80FF00", canvas);
    }






}//class
