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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private ListView listView;
    private ArrayList<String> pairedDeviceArrayList;
    private ArrayAdapter<String> pairedDeviceAdapter;
    public static BluetoothSocket clientSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
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
                    //генерируем socket - поток, через который будут посылаться данные
                    Method m = connectDevice.getClass().getMethod(
                            "createRfcommSocket", new Class[]{int.class});

                    clientSocket = (BluetoothSocket) m.invoke(connectDevice, 1);
                    clientSocket.connect();
                    if(clientSocket.isConnected()) {
                        //если соединение установлено, завершаем поиск
                        bluetoothAdapter.cancelDiscovery();
                    }
                } catch(Exception e) {
                    e.getStackTrace();
                }
            }
        });
    }
}
