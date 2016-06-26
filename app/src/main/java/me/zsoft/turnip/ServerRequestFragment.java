package me.zsoft.turnip;

import org.zeromq.ZMQ;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import java.util.regex.Pattern;

/*
 * This non-UI Fragment will be retained across Activity instances
 */

public class ServerRequestFragment extends Fragment {
    ZMQ.Context mZContext;
    ZMQ.Socket mSocket;
    GameComm mGC;
    //OnServerCommand mCallback;
    Handler mMainHandler;
    MainActivity.SynchReply mSynchReply;

    /*
    public interface OnServerCommand {
        public void updateStatus(String status);

        public void updateBoard(String board);

        public void updateCommand(String command);
        // public void returnGamePort(String port);
    }
    */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);

        // Start the game loop
        new GameLoopTask().execute();
    }

    /*
    @Override
    public void onAttach(Context context) { // for API 23
        super.onAttach(context);
        Activity activity = context instanceof Activity ? (Activity) context : null;


        // make sure the container activity has implemented the callback interface
        try {
            mCallback = (OnServerCommand) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnServerReply");
        }
    }

    @Override
    public void onAttach(Activity activity) { // for older APIs
        super.onAttach(activity);

        // make sure the container activity has implemented the callback interface
        try {
            mCallback = (OnServerCommand) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnServerReply");
        }
    }
    */

    /**
     * This is called when the fragment is going away.  It is NOT called
     * when the fragment is being propagated between activity instances.
     */
    @Override
    public void onDestroy() {
        new ServerDisconnectTask().execute();
        super.onDestroy();
    }

    /*
    public void sendRequest(String request, TextView textView) {
        new SendRequestTask(textView).execute(request);
    }


    private class SendRequestTask extends AsyncTask<String, Void, String> {

        private final TextView textView;

        public SendRequestTask(TextView textView) {
            this.textView = textView;
        }

        @Override
        protected String doInBackground(String... request) {
            mSocket.send(request[0].getBytes(), 0); // rc is true/false?
            String reply = new String(mSocket.recv(0));
            return reply;
        }

        @Override
        protected void onPostExecute(String reply) { // change this
            textView.setText("Reply: " + reply);
        }
    }
    */

    private class GameLoopTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... request) {
            joinGameSession();
            playGame();
            return null;  // needed?
        };

        private void joinGameSession() {
            // connect to the GameServer
            mZContext = ZMQ.context(1);
            mSocket = mZContext.socket(ZMQ.REQ);
            mSocket.connect("tcp://" + Constants.SERVER_IP + ":" + Constants.BROKER_PLAYER_PORT);
            Log.d(Constants.TAG, "Connected to GameServer");

            // join a GameSession using the port supplied by the GameServer
            boolean rc = mSocket.send("join".getBytes(), 0);
            if (!rc) {
                throw new AssertionError("send failed");
            }
            Log.d(Constants.TAG, "Sent the join request");
            String port = new String(mSocket.recv(0));
            Log.d(Constants.TAG, "Received back port " + port);
            mGC = new GameComm(port);
            mGC.connectSession();

            mSocket.send("connected".getBytes(), 0);
            Log.d(Constants.TAG, "Player sent connected message");
            String reply = new String(mSocket.recv(0));
            Log.d(Constants.TAG, "Player received reply: " + reply);
        }

        private void playGame() {
            // this is the main game loop
            String[] command = new String[2];
            Message msg;

            MainActivity activity = (MainActivity) getActivity();
            mMainHandler = activity.getHandler();
            mSynchReply = activity.mSynchReply;

            boolean gameOver = false;
            while (!gameOver) {
                command = mGC.getCommand();
                msg = Message.obtain();
                msg.obj = command;
                synchronized (mSynchReply) {
                    mMainHandler.sendMessage(msg);
                    try {
                        mSynchReply.wait();
                    } catch (InterruptedException e) {
                        Log.d(Constants.TAG, "mSynchReply wait was interrupted");
                    }
                }

                String reply = mSynchReply.getReply();
                mGC.sendReply(reply);
                gameOver = reply.equals("done");
            }


        }

        @Override
        protected void onPostExecute(String port) {
            //..
        }
    }

    private class ServerDisconnectTask extends AsyncTask<Void, Void, Void> {

        public ServerDisconnectTask() {
        }

        @Override
        protected Void doInBackground(Void ...voids) {
            mSocket.close();
            mZContext.term();
            return null;
        }
    }

    private class GameComm {
        ZMQ.Socket gSocket;
        String mName;
        String gPort;

        public GameComm(String port) {
            gPort = port;
        }

        public void connectSession() {
            gSocket = mZContext.socket(ZMQ.REP);
            gSocket.connect("tcp://" + Constants.SERVER_IP + ":" + gPort);
            Log.d(Constants.TAG, "Connected to GameSession on port " + gPort);
        }

        public String[] getCommand() {
            String[] command = new String[2];

            Log.d(Constants.TAG, "Waiting for a command");
            String reply = new String(gSocket.recv(0));
            Log.d(Constants.TAG, "Received GameSession command: " + reply);

            // check if the command has a payload
            int pos = reply.indexOf("|");
            if (pos != -1) {
                // command has a payload so split it out
                command = reply.split(Pattern.quote("|"), 2); // | must be escaped for some unknown reason
            } else {
                // command has no payload
                command[0] = new String(reply);
                command[1] = null;
            }
            return command;
        }

        public void sendReply(String reply) {
            gSocket.send(reply.getBytes(), 0);
            Log.d(Constants.TAG, "Player sent reply: " + reply);
        }

        public void setName(String name) {
            mName = name;
        }
    }

    /*
    public void sendReply(String reply) {
        mGC.sendReply(reply);
    }
    */
}
