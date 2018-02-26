package com.example.uceda.puigball;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.Toast;

//Empezará y detendrá el hilo en SnakeView y mostrará su contenido (el juego en sí)
public class SnakeActivity extends Activity implements SimpleGestureFilter.SimpleGestureListener {

    private SimpleGestureFilter detector;

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

        // Detect touched area
        detector = new SimpleGestureFilter(this,this);
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent me){
        // Call onTouchEvent of SimpleGestureFilter class
        this.detector.onTouchEvent(me);
        return super.dispatchTouchEvent(me);
    }
    @Override
    public void onSwipe(int direction) {
        String str = "";

        switch (direction) {

            case SimpleGestureFilter.SWIPE_RIGHT :
                str = "Swipe Right";
                snakeView.setDireccion(SnakeView.Direction.RIGHT);
                break;
            case SimpleGestureFilter.SWIPE_LEFT :
                str = "Swipe Left";
                snakeView.setDireccion(SnakeView.Direction.LEFT);
                break;
            case SimpleGestureFilter.SWIPE_DOWN :
                str = "Swipe Down";
                snakeView.setDireccion(SnakeView.Direction.DOWN);
                break;
            case SimpleGestureFilter.SWIPE_UP :
                str = "Swipe Up";
                snakeView.setDireccion(SnakeView.Direction.UP);
                break;

        }
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDoubleTap() {
        Toast.makeText(this, "Double Tap", Toast.LENGTH_SHORT).show();
    }
}


