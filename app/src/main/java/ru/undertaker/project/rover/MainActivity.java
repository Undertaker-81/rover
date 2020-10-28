package ru.undertaker.project.rover;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import android.Manifest;
import android.bluetooth.*;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private ListView listView;
    private ArrayList<String> pairedDeviceArrayList;
    private ArrayAdapter<String> pairedDeviceAdapter;
    public static BluetoothSocket clientSocket;
    private OutputStream outStrem;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        connectBt();
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectBt();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        connectBt();
    }

    protected void connectBt(){
        //цепляем кнопку поиска БТ
        Button buttonStartFind =  findViewById(R.id.buttonStartSearch);
        //цепляем layout, в котором будут отображаться найденные устройства
        listView =  findViewById(R.id.list_device);
        //устанавливаем действие на клик
        buttonStartFind.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //если разрешения получены (функция ниже)
                if(permissionGranted()) {
                    //адаптер для управления блютузом
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if(bluetoothEnabled()) { //если блютуз включен (функция ниже)
                        findArduino(); //начать поиск устройства (функция ниже)
                    }
                }
            }
        });
    }

    private boolean permissionGranted() {
        //если оба разрешения получены, вернуть true
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.BLUETOOTH) == PermissionChecker.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(),  Manifest.permission.BLUETOOTH_ADMIN) == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN}, 0);
            return false;
        }
    }

    private boolean bluetoothEnabled() {
//если блютуз включен, вернуть true, если нет, вежливо попросить пользователя его включить
        if(bluetoothAdapter.isEnabled()) {
            return true;
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
            return false;
        }
    }
    private void findArduino() {
        //получить список доступных устройств
        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();


        if (pairedDevice.size() > 0) { //если есть хоть одно устройство
            pairedDeviceArrayList = new ArrayList<>(); //создать список
            for(BluetoothDevice device: pairedDevice) {
                //добавляем в список все найденные устройства
                //формат: "уникальный адрес/имя"
                pairedDeviceArrayList.add(device.getAddress() + "/" + device.getName());
            }
        }
        pairedDeviceAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pairedDeviceArrayList);

        listView.setAdapter(pairedDeviceAdapter);
        //на каждый элемент списка вешаем слушатель
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //через костыль получаем адрес
                String itemMAC =  listView.getItemAtPosition(i).toString().split("/", 2)[0];
                //получаем класс с информацией об устройстве
                BluetoothDevice connectDevice = bluetoothAdapter.getRemoteDevice(itemMAC);
                try {
                    clientSocket = connectDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                    clientSocket.connect();
                    outStrem = clientSocket.getOutputStream();

                    if(clientSocket.isConnected()) {
                        //если соединение установлено, завершаем поиск
                        bluetoothAdapter.cancelDiscovery();
                        listView.setVisibility(View.INVISIBLE);
                    }

                } catch(Exception e) {
                    e.getStackTrace();
                }
            }
        });


        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView_right);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                sendData("$" + angle + " " + strength + ";");// do whatever you want
            }
        });
    }
    public void sendData(String message) {
        byte[] msgBuffer = message.getBytes();


        try {
            outStrem.write(msgBuffer);
        } catch (IOException e) {}
    }

    public void cancel(){
        try {
            outStrem.close();
        }catch(IOException e){}
    }
}
