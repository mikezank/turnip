package me.zsoft.turnip;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity
        implements NameFragment.OnNameEntered {

    static ServerRequestFragment mRequestFragment;
    FragmentManager mFragmentManager;
    static TextView mStatusText;
    static TextView mCommandText;
    static EditText mInputText;
    static Button mPickButton;
    static Button mGuessButton;
    public static SynchReply mSynchReply;
    static String mEnteredText;
    static String mGuess;
    static Context mContext;
    static Letters mLetters;
    static GridLayout mBoardLayout;
    static PuzzleBoard mBoard;
    static String mLetter;
    static String mPlayerName;
    static boolean mPlayerIsMale;

    private final MainHandler mHandler = new MainHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSynchReply = new SynchReply();
        setContentView(R.layout.activity_main);
        mStatusText = (TextView) findViewById(R.id.statusText);
        mCommandText = (TextView) findViewById(R.id.commandText);
        mInputText = (EditText) findViewById(R.id.inputText);
        mBoardLayout = (GridLayout) findViewById(R.id.boardLayout);

        mPickButton = (Button) findViewById(R.id.pickButton);
        mPickButton.setEnabled(false);
        mPickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEnteredText = mInputText.getText().toString().toUpperCase();
                mLetter = new String(mEnteredText);
                MainHandler.sendReply(mEnteredText);
                v.setEnabled(false);
                mStatusText.requestFocus();  // doesn't work
            }
        });

        mGuessButton = (Button) findViewById(R.id.guessButton);
        mGuessButton.setEnabled(false);
        mGuessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGuess = mInputText.getText().toString();
                MainHandler.sendReply(mGuess);
                v.setEnabled(false);
            }
        });

        mContext = this;
        Bitmap fullAlphabet = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.alphabet);
        mLetters = new Letters(fullAlphabet);

        // code to set up UI elements
        // ...

        mFragmentManager = getFragmentManager();
        // Make sure ServerRequestFragment is retained or created
        mRequestFragment = (ServerRequestFragment) mFragmentManager.findFragmentByTag("request");
        if (mRequestFragment == null) {
            mRequestFragment = new ServerRequestFragment();
            mFragmentManager.beginTransaction().add(mRequestFragment, "request").commit();
        }

        // start the NameFragment
        DialogFragment nameFragment = NameFragment.newInstance("x", "y");
        nameFragment.show(mFragmentManager, "name");
    }

    @Override
    protected void onDestroy() {
        // Make sure ServerRequestFragment is destroyed
        mRequestFragment = (ServerRequestFragment) mFragmentManager.findFragmentByTag("request");
        if (mRequestFragment != null) {
            mFragmentManager.beginTransaction().remove(mRequestFragment).commit();
        }
        super.onDestroy();
    }

    // Implemented methods for OnServerCommand interface -- no! no longer using this
    public static void updateStatus(String status) {
        mStatusText.setText(status);
    }

    public static void displayNewBoard(String puzzle) {
        // display empty game board
        mBoard = new PuzzleBoard(puzzle);
    }

    public static void updateBoard(String payload) {
        // update game board with new letter
        mBoard.turnLetters(mLetter, payload);
    }

    public static void updateCommand(String command) {mCommandText.setText(command); }

    public static String getPlayerName() {
        return mPlayerName;
    }

    private static class PuzzleBoard {

        ImageView[] pImages;

        PuzzleBoard(String puzzle) {
            ViewGroup.MarginLayoutParams mp;
            ImageView iv;

            pImages = new ImageView[puzzle.length()];

            for (int n=0; n<puzzle.length(); n++) {
                iv = new ImageView(mContext);
                pImages[n] = iv;
                iv.setImageBitmap(mLetters.getPic(puzzle.substring(n, n+1)));
                mBoardLayout.addView(iv);
            }

            for (int n=0; n<pImages.length; n++) {
                mp = (ViewGroup.MarginLayoutParams) pImages[n].getLayoutParams();
                mp.setMargins(1, 1, 1, 1);
                pImages[n].setLayoutParams(mp);
            }
        }

        public void turnLetters(String letter, String payload) {
            String[] locations;
            Bitmap newpic;

            newpic = mLetters.getPic(letter.toUpperCase());
            locations = payload.split("-");
            for (int n=0; n<locations.length; n++) {
                pImages[Integer.decode(locations[n])].setImageBitmap(newpic);
            }
        }
    }

    public static class MainHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MainHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            String[] command = new String[2];

            MainActivity activity = mActivity.get();
            if (activity != null) {
                Log.d(Constants.TAG, String.format("Handler.handleMessage(): msg=%s", msg));
                command = (String[]) msg.obj;

                switch (command[0]) {
                    case "ready":
                        sendReply(getPlayerName());
                        //mGC.setName("Mike");
                        updateCommand("Ready to play!");
                        break;
                    case "board":
                        displayNewBoard(command[1]);
                        sendReply("ok");
                        break;
                    case "update":
                        sendReply("ok");
                        updateBoard(command[1]);
                        break;
                    case "done":
                        sendReply("done");
                        updateStatus("Game over. You lost.");
                        break;
                    case "won":
                        sendReply("done");
                        updateStatus("Game over.  You won!");
                        break;
                    case "lost":
                        sendReply("done");
                        updateStatus("Game over.  You lost.");
                        break;
                    case "pick":
                        mPickButton.setEnabled(true);
                        mPickButton.setText("Pick");
                        mInputText.setText("");
                        updateCommand("Pick a letter");
                        break;
                    case "chosen":
                        sendReply("ok");
                        updateStatus("That letter was already chosen");
                        break;
                    case "timeout":
                        sendReply("ok");
                        updateStatus("Took too long for your turn.");
                        break;
                    case "found":
                        if (command[1].equals("0")) {
                            Log.d(Constants.TAG, "Found 0. Shouldn't get to this code!");
                            updateStatus("No, there is no letter " + mEnteredText);
                        } else if (command[1].equals("1")) {
                            updateStatus("Yes, we have one letter " + mEnteredText);
                        } else {
                            updateStatus("Yes, we have " + command[1] + " " + mEnteredText + "'s");
                        }
                        sendReply("ok");
                        break;
                    case "guess":
                        mPickButton.setEnabled(true);
                        mPickButton.setText("Guess");
                        updateCommand("Guess the puzzle");
                        break;
                    case "picked2":
                        updateStatus("Letter " + command[1] + " was picked again.");
                        sendReply("ok");
                        break;
                    case "picked":
                        updateStatus("Letter " + command[1] + " was picked.");
                        mLetter = command[1];
                        sendReply("ok");
                        break;
                    case "timedout":
                        updateStatus("Player timed out.");
                        sendReply("ok");
                        break;
                    default:
                        Log.d(Constants.TAG, "Illegal command received: " + command[0]);
                }
            }
        }

        public static void sendReply(String reply) {
            synchronized (mSynchReply) {
                mSynchReply.setReply(reply);
                mSynchReply.notify();
            }
        }
    }


    public Handler getHandler() {
        return mHandler;
    }


    public class SynchReply {
        private String reply;

        public String getReply () {
            return reply;
        }

        public void setReply(String reply) {
            this.reply = reply;
        }
    }

    public void onNameEntered(String name, boolean isMale) {
        mPlayerName = name;
        mPlayerIsMale = isMale;
    }
}
