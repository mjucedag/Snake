package com.example.uceda.puigball;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

//Empezará y detendrá el hilo en SnakeView y mostrará su contenido (el juego en sí)
public class SnakeActivity extends Activity {

    SnakeView snakeView;
    // Se inicia en onCreate una vez que tengas más detalles del dispositivo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Saca el ancho y el alto de la pantalla
        Display display = getWindowManager().getDefaultDisplay();

        // Carga la resolución del punto
        Point size = new Point();
        display.getSize(size);

        // Inicializo mi serpiente
        snakeView = new SnakeView(this, size);

        // Vista por defecto
        setContentView(snakeView);
    }

    // Empieza el hilo en snakeView cuando esta actividad sea mostrada al jugador
    @Override
    protected void onResume() {
        super.onResume();
        snakeView.reanudacion();
    }

    // Se asegura de que el hilo en snakeView se detenga cuando Activity esté cerrada.
    @Override
    protected void onPause() {
        super.onPause();
        snakeView.pausa();
    }
}


