package com.example.uceda.puigball;

/**
 * Created by uceda on 25/02/2018.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Random;

//La vista del juego. Contendrá el hilo donde se ejecuta el juego completamente.
//hereda para añadir instalaciones de dibujo más flexibles y Runnable para la funcionalidad del hilo.
class SnakeView extends SurfaceView implements Runnable {

    private Thread hilo = null;
    // Esta variable determina cuando el juego está activo
    // Declarada como volátil porque puede ser accesible desde dentro y fuera del hilo
    private volatile boolean juegoActivo;

    private Canvas canvas;
    private SurfaceHolder holder;     // Hace el trazo
    private Paint pinta;            //controla los colores
    private Context contexto;

    // sonido
    private SoundPool sonido;
    private int get_mouse_sound = -1;
    private int dead_sound = -1;

    // para seguir moviendo
    public enum Direction {UP, RIGHT, DOWN, LEFT}
    private Direction direccion = Direction.LEFT;// empieza moviendose hacia la derecha

    // La resolución de la pantalla
    private int anchoPantalla;
    private int altoPantalla;

    // controla la pausa entre actualizaciones
    private long tiempoSiguienteFrame;
    private final long TIMES = 10;     //actualiza el juego 10 veces por segundo
    private final long MILISEGUNDOS = 1000;  // Hay 1000 milisegundos en un segundo
    private int puntuacion; // puntuacion actual

    // la localizacion de la cuadricula en todos los segmentos
    private int[] snakeXs;
    private int[] snakeYs;

    private int snakeLength;
    private int mouseX;
    private int mouseY;
    private int snakeSize; //tamaño en pixeles
    private final int NUM_BLOQUES_ANCHO = 40;  //tamaño del juego en pixeles
    private int numBloquesAlto; //determinado dinámicamente

    public SnakeView(Context context, Point size) {
        super(context);
        contexto = context;
        anchoPantalla = size.x;
        altoPantalla = size.y;

        //determina el tamaño de cada bloque en el tablero de juego
        snakeSize = anchoPantalla / NUM_BLOQUES_ANCHO;
        //calcula cuantos bloques del mismo tamaño encajan en la altura de la pantalla
        numBloquesAlto = ((altoPantalla)) / snakeSize;
        cargarSonido();//pone el sonido
        holder = getHolder();
        pinta = new Paint();

        snakeXs = new int[300];
        snakeYs = new int[300];
        empiezaJuego();
    }

    @Override
    public void run() {
        while (juegoActivo) {
            if(confirmarParaActualizar()) {
                actualizaJuego();
                dibujaJuego();
            }
        }
    }

    public void cargarSonido() {
        sonido = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try {
            // usa contexto porque se refiere a la propia Activity
            AssetManager assetManager = contexto.getAssets();
            AssetFileDescriptor descriptor;

            // prepara los dos sonidos en la memoria
            descriptor = assetManager.openFd("get_mouse_sound.ogg");
            get_mouse_sound = sonido.load(descriptor, 0);

            descriptor = assetManager.openFd("death_sound.ogg");
            dead_sound = sonido.load(descriptor, 0);


        } catch (IOException e) {
            // Error
        }
    }

    public void empiezaJuego() {
        // empieza con una cabeza en mitad de la pantalla
        direccion = Direction.LEFT;
        snakeLength = 20;
        snakeXs[0] = NUM_BLOQUES_ANCHO / 2;
        snakeYs[0] = numBloquesAlto / 2;
        empiezaMouse(); // y un ratón para comer
        puntuacion = 0;
        tiempoSiguienteFrame = System.currentTimeMillis();
    }

    public void empiezaMouse() {
        Random random = new Random();
        mouseX = random.nextInt(NUM_BLOQUES_ANCHO - 1) + 1;
        mouseY = random.nextInt(numBloquesAlto - 1) + 1;
    }

    private void comeMouse(){
        snakeLength++;
        empiezaMouse();
        puntuacion = puntuacion + 1;
        sonido.play(get_mouse_sound, 1, 1, 0, 0, 1);
    }

    private void mueveSnake(){
        
        for (int i = snakeLength; i > 0; i--) {
            snakeXs[i] = snakeXs[i - 1];
            snakeYs[i] = snakeYs[i - 1];
        }
        // mover la cabeza en la dirección apropiada
        switch (direccion) {
            case UP:
                snakeYs[0]--;
                break;
            case RIGHT:
                snakeXs[0]++;
                break;
            case DOWN:
                snakeYs[0]++;
                break;
            case LEFT:
                snakeXs[0]--;
                break;
        }
    }

    private boolean estaMuerta(){
        
        boolean muerta = false;
        // choca con la pared
        if (snakeXs[0] == -1) muerta = true;
        if (snakeXs[0] >= NUM_BLOQUES_ANCHO) muerta = true;
        if (snakeYs[0] == -1) muerta = true;
        if (snakeYs[0] == numBloquesAlto) muerta = true;
        // se come a sí misma
        for (int i = snakeLength - 1; i > 0; i--) {
            if ((i > 4) && (snakeXs[0] == snakeXs[i]) && (snakeYs[0] == snakeYs[i])) {
                muerta = true;
            }
        }
        
        return muerta;
    }

    public void actualizaJuego() {
        // cuando la cabeza de la serpiente toca al ratón
        if (snakeXs[0] == mouseX && snakeYs[0] == mouseY) {
            comeMouse();
        }

        mueveSnake();

        if (estaMuerta()) {
            //empieza de nuevo
            sonido.play(dead_sound, 1, 1, 0, 0, 1);

            alertOneButton();
        }
    }

    /*
 * AlertDialog with one button.
 */
    public void alertOneButton() {

        Activity activity = (Activity) contexto;

        //Lanzar el dialogo en el Hilo principal | UI  = User Interface
        activity.runOnUiThread(new Runnable() {
            public void run() {
                new AlertDialog.Builder(contexto)
                        .setTitle("Snake")
                        .setMessage("Tu serpiente ha muerto. ¿Quieres revivirla?")
                        .setIcon(R.drawable.snakeicon)
                        .setPositiveButton("Resucita", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                SnakeView.this.empiezaJuego();
                                SnakeView.this.reanudacion();
                            }
                        }).show();
                SnakeView.this.pausa();
            }
        });


    }

    public void dibujaJuego() {
        //dibuja el lienzo
        if (holder.getSurface().isValid()) {
            canvas = holder.lockCanvas();
            canvas.drawColor(Color.argb(255, 120, 197, 87)); //el cesped
            pinta.setColor(Color.argb(255, 255, 255, 255));//la serpiente
            pinta.setTextSize(60);
            canvas.drawText("Puntuación:" + puntuacion, 10, 30, pinta);


            //dibuja la serpiente
            for (int i = 0; i < snakeLength; i++) {
                if(i==0){
                    pinta.setColor(Color.BLACK); //la cabeza ...
                }else{
                    pinta.setColor(Color.argb(255, 255, 255, 255));//la serpiente
                }
                canvas.drawRect(snakeXs[i] * snakeSize,
                        (snakeYs[i] * snakeSize),
                        (snakeXs[i] * snakeSize) + snakeSize,
                        (snakeYs[i] * snakeSize) + snakeSize,
                        pinta);
            }

            pinta.setColor(Color.argb(255, 255, 0, 0));
            //dibuja la fruta
            canvas.drawRect(mouseX * snakeSize,
                    (mouseY * snakeSize),
                    (mouseX * snakeSize) + snakeSize,
                    (mouseY * snakeSize) + snakeSize,
                    pinta);

            // dibuja el frame
            holder.unlockCanvasAndPost(canvas);
        }
    }
//chequea para ver si la variable tiempoSiguienteFrame ha sido superada por el timpo real.
    //si lo hace, una nueva hora es fijada y guardada en esa variable
    //si no false es devuelto y el siguiente cuadro es retrasado hasta que sea hora.
    public boolean confirmarParaActualizar() {

        // actualiza el lienzo
        if(tiempoSiguienteFrame <= System.currentTimeMillis()){
            // ha pasado una decima de segundo

            // establece cuando la próxima actualización será disparada
            tiempoSiguienteFrame =System.currentTimeMillis() + MILISEGUNDOS / TIMES;

            // devuelve true para que la actualizaacion y las funciones de dibjo se ejecuten
            return true;
        }
        return false;
    }


    public void pausa() {
        juegoActivo = false;
        try {
            hilo.join();
        } catch (InterruptedException e) {
            // Error
        }
    }

    public void reanudacion() {
        juegoActivo = true;
        hilo = new Thread(this);
        hilo.start();
    }

    public void setDireccion(Direction direccion) {
        this.direccion = direccion;
    }

    //se encarga de los toques en pantalla
 /*   @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (motionEvent.getX() >= anchoPantalla / 2) {
                    switch(direccion){
                        case UP:
                            direccion = Direction.RIGHT;
                            break;
                        case RIGHT:
                            direccion = Direction.DOWN;
                            break;
                        case DOWN:
                            direccion = Direction.LEFT;
                            break;
                        case LEFT:
                            direccion = Direction.UP;
                            break;
                    }
                } else {
                    switch(direccion){
                        case UP:
                            direccion = Direction.LEFT;
                            break;
                        case LEFT:
                            direccion = Direction.DOWN;
                            break;
                        case DOWN:
                            direccion = Direction.RIGHT;
                            break;
                        case RIGHT:
                            direccion = Direction.UP;
                            break;
                    }
                }
        }
        return true;
    }*/
}
