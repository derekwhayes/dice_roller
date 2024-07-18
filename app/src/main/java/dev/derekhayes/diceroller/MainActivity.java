package dev.derekhayes.diceroller;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.CountDownTimer;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements RollLengthDialogFragment.OnRollLengthSelectedListener {

    public static final int MAX_DICE = 3;

    private int mVisibleDice;
    private Dice[] mDice;
    private ImageView[] mDiceImageViews;
    private Menu mMenu;
    private CountDownTimer mTimer;
    private long mTimerLength = 2000;
    private int mCurrentDie;
    private int mInitX;
    private int mInitY;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mDice = new Dice[MAX_DICE];
        for (int i = 0; i < MAX_DICE; i++) {
            mDice[i] = new Dice(i + 1);
        }

        mDiceImageViews = new ImageView[MAX_DICE];
        mDiceImageViews[0] = findViewById(R.id.dice1);
        mDiceImageViews[1] = findViewById(R.id.dice2);
        mDiceImageViews[2] = findViewById(R.id.dice3);

        mVisibleDice = MAX_DICE;

        showDice();

        registerForContextMenu(mDiceImageViews[0]);

        for(int i = 0; i < mDiceImageViews.length; i++) {
            registerForContextMenu(mDiceImageViews[i]);
            mDiceImageViews[i].setTag(i);
        }

        mDiceImageViews[0].setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mInitX = (int) event.getX();
                    mInitY = (int) event.getY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int x = (int) event.getX();
                    int y = (int) event.getY();

                    if (Math.abs(x - mInitX) >= 100 || Math.abs(y - mInitY) >= 100) {
                        if (x > mInitX || y > mInitY) {
                            mDice[0].addOne();
                        }
                        else {
                            mDice[0].subtractOne();
                        }
                        showDice();
                        mInitX = x;
                        mInitY = y;
                    }

                    return true;
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar_menu, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    private void showDice() {
        for (int i = 0; i < mVisibleDice; i++) {
            Drawable diceDrawable = ContextCompat.getDrawable(this, mDice[i].getImageId());
            mDiceImageViews[i].setImageDrawable(diceDrawable);
            mDiceImageViews[i].setContentDescription(Integer.toString(mDice[i].getNumber()));
        }
    }

    @Override
    public void onRollLengthClick(int which) {
        mTimerLength = 1000L * (which + 1);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_one) {
            changeDiceVisibility(1);
            showDice();
            return true;
        }
        else if (item.getItemId() == R.id.action_two) {
            changeDiceVisibility(2);
            showDice();
            return true;
        }
        else if (item.getItemId() == R.id.action_three) {
            changeDiceVisibility(3);
            showDice();
            return true;
        }
        else if (item.getItemId() == R.id.action_stop) {
            mTimer.cancel();
            item.setVisible(false);
            return true;
        }
        else if (item.getItemId() == R.id.action_roll) {
            rollDice();
            return true;
        }
        else if (item.getItemId() == R.id.action_roll_length) {
            RollLengthDialogFragment dialog = new RollLengthDialogFragment();
            dialog.show(getSupportFragmentManager(), "rollLengthDialog");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void rollDice() {
        mMenu.findItem(R.id.action_stop).setVisible(true);

        if (mTimer != null) {
            mTimer.cancel();
        }

        mTimer = new CountDownTimer(mTimerLength, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                for (int i = 0; i < mVisibleDice; i++) {
                    mDice[i].roll();
                }
                showDice();
            }

            @Override
            public void onFinish() {
                mMenu.findItem(R.id.action_stop).setVisible(false);
            }
        }.start();
    }

    private void changeDiceVisibility(int numVisible) {
        mVisibleDice = numVisible;

        for (int i = 0; i < numVisible; i++) {
            mDiceImageViews[i].setVisibility(View.VISIBLE);
        }

        for (int i = numVisible; i < MAX_DICE; i++) {
            mDiceImageViews[i].setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        mCurrentDie = (int) v.getTag();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_one) {
            mDice[mCurrentDie].addOne();
            showDice();
            return true;
        }
        else if (item.getItemId() == R.id.subtract_one) {
            mDice[mCurrentDie].subtractOne();
            showDice();
            return true;
        }
        else if (item.getItemId() == R.id.roll) {
            rollDice();
            return true;
        }

        return super.onContextItemSelected(item);
    }
}