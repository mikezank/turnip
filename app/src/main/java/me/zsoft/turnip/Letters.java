package me.zsoft.turnip;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by mikezank on 6/22/16.
 */
public class Letters {

    static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static final int IMAGE_WIDTH = 267;
    static final int IMAGE_HEIGHT = 267;
    static final int NEW_WIDTH = 35;
    static final int NEW_HEIGHT = 35;
    static final int LETTER_HGAP = 120;
    static final int LETTER_VGAP = 60;

    Bitmap fullAlphabet;
    Bitmap letterPic;
    HashMap coordHash;
    //GridLayout grid;


    public Letters(Bitmap bitmap) {
        fullAlphabet = bitmap;
        coordHash = new HashMap();

        for (int n=0; n<26; n++) {
            String letter = LETTERS.substring(n, n+1);
            int row = n / 6;
            int col = n - row*6;
            Log.d("BoardAnimate", "Row, Col: " + row + " " + col);
            letterPic = Bitmap.createBitmap(fullAlphabet, col*(IMAGE_WIDTH + LETTER_HGAP),
                    row*(IMAGE_HEIGHT + LETTER_VGAP), IMAGE_WIDTH, IMAGE_HEIGHT);
            coordHash.put(letter, resizedPic(letterPic));
        }

        // create missing letter "_"
        letterPic = Bitmap.createBitmap(fullAlphabet, IMAGE_WIDTH + LETTER_HGAP + 50,
                IMAGE_HEIGHT - 50, IMAGE_WIDTH - 100, 50);
        coordHash.put("_", resizedPic(letterPic));

        // create space
        letterPic = Bitmap.createBitmap(fullAlphabet, IMAGE_WIDTH + LETTER_HGAP + 50,
                IMAGE_HEIGHT + LETTER_VGAP + 50, 10, 10);
        coordHash.put(" ", resizedPic(letterPic));
    }

    private Bitmap resizedPic(Bitmap pic) {
        return Bitmap.createScaledBitmap(pic, NEW_WIDTH, NEW_HEIGHT, false);
    }

    public Bitmap getPic(String letter) { return (Bitmap) coordHash.get(letter); }
}
