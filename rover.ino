#include <Wire.h> 
#include <SoftwareSerial.h>
#define R_A_IA 9
#define R_A_IB 10
#define L_B_IA 11
#define L_B_IB 12
SoftwareSerial mySerial(8, 7); // RX, TX
char incomingbyte; // переменная для приема данных

int intData[2];     // массив численных значений после парсинга
boolean recievedFlag;
int dutyR, dutyL;
int signalX, signalY;
int dataX, dataY;



void setup() 
 {
   Serial.begin(9600); 
   mySerial.begin(9600);
  //RIGHT
  pinMode(R_A_IA,OUTPUT); 
  pinMode(R_A_IB,OUTPUT);
 
  //LEFT
  pinMode(L_B_IA,OUTPUT);
  pinMode(L_B_IB,OUTPUT);
 
  
 }


void loop() 
 {
  parsing();               // функция парсинга
  if (recievedFlag) {     // если получены данные
    recievedFlag = false;
    dataX = intData[0];
    dataY = intData[1];
    //вперед
    if ( dataX >= 85 && dataX <= 95){
      go_forward();
       Serial.println(dataX);

    }
    //назад
    if ( dataX >= 265 && dataX <= 275){
      go_back();
    }
//left
    if ( dataX >= 95 && dataX <= 180){
      go_left();
    }

    if ( dataX >= 0 && dataX <= 85){
      go_right();
    }

    if (dataX == 0) {
      stop_robot();
    }
    // Устанавливаем курсор на вторую строку и нулевой символ.
 // lcd.setCursor(0, 1);
  // Выводим на экран
//  lcd.print(dataX + " " + dataY);        

       }
   }
   
boolean getStarted;
byte index;
String string_convert = "";
void parsing() {
  if (mySerial.available() > 0) {
    char incomingByte = mySerial.read();        // обязательно ЧИТАЕМ входящий символ
    if (getStarted) {                         // если приняли начальный символ (парсинг разрешён)
      if (incomingByte != ' ' && incomingByte != ';') {   // если это не пробел И не конец
        string_convert += incomingByte;       // складываем в строку
      } else {                                // если это пробел или ; конец пакета
        intData[index] = string_convert.toInt();  // преобразуем строку в int и кладём в массив
        string_convert = "";                  // очищаем строку
        index++;                              // переходим к парсингу следующего элемента массива
      }
    }
    if (incomingByte == '$') {                // если это $
      getStarted = true;                      // поднимаем флаг, что можно парсить
      index = 0;                              // сбрасываем индекс
      string_convert = "";                    // очищаем строку
    }
    if (incomingByte == ';') {                // если таки приняли ; - конец парсинга
      getStarted = false;                     // сброс
      recievedFlag = true;                    // флаг на принятие
    }
  }
}
  void go_forward(){
    Serial.println("forward");
  //motors LEFT
  digitalWrite(L_B_IA, LOW);  
  digitalWrite(L_B_IB, HIGH); 
  //motors RIGHT
  digitalWrite(R_A_IA, LOW);
  digitalWrite(R_A_IB, HIGH);
}

void go_back(){
  //motors LEFT
  digitalWrite(L_B_IA, HIGH);  
  digitalWrite(L_B_IB, LOW); 
  //motors RIGHT
  digitalWrite(R_A_IA, HIGH);
  digitalWrite(R_A_IB, LOW);
}

void go_right(){
  //motors LEFT
  digitalWrite(L_B_IA, LOW);  
  digitalWrite(L_B_IB, HIGH); 
  //motors RIGHT
  digitalWrite(R_A_IA, LOW);
  digitalWrite(R_A_IB, LOW);
}

void go_left(){
  //motors LEFT
  digitalWrite(L_B_IA, LOW);  
  digitalWrite(L_B_IB, LOW); 
  //motors RIGHT
  digitalWrite(R_A_IA, LOW);
  digitalWrite(R_A_IB, HIGH);
}

void stop_robot(){
  //motors LEFT
  digitalWrite(L_B_IA, LOW);  
  digitalWrite(L_B_IB, LOW); 
  //motors RIGHT
  digitalWrite(R_A_IA, LOW);
  digitalWrite(R_A_IB, LOW);
}
