package chief.pong;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;


public class MainActivity extends AppCompatActivity {



    //The y coordinate touched by the player, used to move the player paddle.
    private int yTouchPosition;
    private int aiScore = 0;
    private int playerScore = 0;
    private volatile boolean moving = true;
    private int dx = 1;
    private int dy = 1;

    Rect playerPaddle;
    Rect aiPaddle;
    RectF ball;


    DisplayMetrics metrics = new DisplayMetrics();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GameView gameView = new GameView(this);
        setContentView(gameView);

        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        setObjects();
    }

    public void setObjects() {
        playerPaddle.set(20, metrics.heightPixels / 2 - 75, 35, metrics.heightPixels / 2 + 75);
        aiPaddle.set(metrics.widthPixels - 35, metrics.heightPixels / 2 - 75,
                metrics.widthPixels - 20, metrics.heightPixels / 2 + 75);
        ball.set(metrics.widthPixels / 2 - 15, metrics.heightPixels / 2 - 15,
                metrics.widthPixels / 2 + 15, metrics.heightPixels / 2 + 15);
    }

    class GameView extends View implements  View.OnTouchListener {
        public GameView(Context context) {
            super(context);
            setOnTouchListener(this);
            setWillNotDraw(false);

            playerPaddle = new Rect(0, 0, 15, 150);
            aiPaddle = new Rect(0, 0, 15, 150);
            ball = new RectF(0, 0, 30, 30);
        }

        public void moveBall () {
            ball.offset(1, -1);
        }

        public class BallMover extends Thread {

            @Override
            public void run() {
                while (moving) {
                    try {
                        postInvalidate();

                        Thread.sleep(1);

                        move();

                        //Changing directions if side is impacted.
                        if (ball.top <= 0) {
                            setDy(1);
                        }

                        if (ball.bottom >= getHeight()) {
                            setDy(-1);
                        }

                        //The ai scores.
                        if (ball.centerX() < playerPaddle.left) {
                            stopBall();
                            aiScore++;
                        }

                        //The player scores.
                        if (ball.centerX() > aiPaddle.right) {
                            stopBall();
                            playerScore++;
                        }

                        //Collision with the player paddle.
                        if (ball.bottom == playerPaddle.top && ball.left < playerPaddle.right
                                || ball.left == playerPaddle.right && ball.top <= playerPaddle.bottom
                                && ball.bottom >= playerPaddle.top
                                || ball.top == playerPaddle.bottom
                                && ball.left < playerPaddle.right) {
                            setDx(1);
                        }

                        //Collision with the ai paddle.
                        if (ball.bottom == aiPaddle.top && ball.right > aiPaddle.left
                                || ball.right == aiPaddle.left && ball.top <= aiPaddle.bottom
                                && ball.bottom >= aiPaddle.top || ball.top == aiPaddle.bottom
                                && ball.right > aiPaddle.left) {
                            setDx(-1);
                        }

                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }

            //This method moves the ball.
            public void move() {
                 ball.offset(dx, dy);
}
            //Called to change the x direction of the ball.
            public void setDx (int x) {
                dx = x;
            }

            //Called to change the y direction of the ball.
            public void setDy (int y) {
                dy = y;
            }

            //Called when player or ai scores. The game board is reset and the ball starts moving
            //again after two seconds.
            public void stopBall() {
                moving = false;
                postInvalidate();
                setObjects();
                try {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                moving = true;
            }
        }



        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            //Sets the background to a horrible shade of cyan.
            canvas.drawColor(Color.CYAN);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            //Draw the paddles.
            paint.setColor(Color.BLACK);
            canvas.drawRect(playerPaddle, paint);
            canvas.drawRect(aiPaddle, paint);

            //Draw the ball.
            paint.setColor(Color.RED);
            canvas.drawOval(ball, paint);

            //Starts the thread that moves the ball.
            getBallMover();
            if (mBallMover.isAlive() == false)
            mBallMover.start();


        }

        BallMover mBallMover = new BallMover();

        public BallMover getBallMover() {
            return mBallMover;
        }


        //Move the player paddle on touch.
        public boolean onTouch(View v, MotionEvent motionEvent) {

            if (v == this) {
                yTouchPosition = (int) motionEvent.getY();


                invalidate();
                int difference = playerPaddle.centerY() - yTouchPosition;

                //Move the paddle up and down.
                if (difference > 0 && playerPaddle.top > 0 &&
                        playerPaddle.bottom < getHeight()) {

                    playerPaddle.offset(0, -10);
                }

                if (difference < 0 && playerPaddle.top > 0 &&
                        playerPaddle.bottom < getHeight()) {

                    playerPaddle.offset(0, 10);
                }

                //Prevent the paddle from going off screen.
                if (playerPaddle.top <= 0 && Math.abs(difference) > playerPaddle.centerX()) {
                    playerPaddle.offset(0, 10);
                }

                if (playerPaddle.bottom >= getHeight() &&
                        Math.abs(difference) > playerPaddle.centerX()) {
                    playerPaddle.offset(0, -10);
                }
            }
            return true;
        }
    }
}







