package me.zsoft.turnip;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;


/**
 * A simple {@link DialogFragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NameFragment.OnNameEntered} interface
 * to handle interaction events.
 * Use the {@link NameFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NameFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnNameEntered mListener;

    public NameFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NameFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NameFragment newInstance(String param1, String param2) {
        NameFragment fragment = new NameFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_name, container, false);
        Button enterButton = (Button) v.findViewById(R.id.enterButton);
        final EditText nameText = (EditText) v.findViewById(R.id.nameText);
        final RadioButton maleRadio = (RadioButton) v.findViewById(R.id.maleRadio);
        RadioButton femaleRadio = (RadioButton) v.findViewById(R.id.femaleRadio);

        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onNameEntered(nameText.getText().toString(), maleRadio.isChecked());
                dismiss();
            }
        });

        return v;
    }


    @Override
    public void onAttach(Context context) { // for API 23
        super.onAttach(context);
        Activity activity = context instanceof Activity ? (Activity) context : null;


        // make sure the container activity has implemented the callback interface
        try {
            mListener = (OnNameEntered) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnServerReply");
        }
    }

    @Override
    public void onAttach(Activity activity) { // for older APIs
        super.onAttach(activity);

        // make sure the container activity has implemented the callback interface
        try {
            mListener = (OnNameEntered) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnServerReply");
        }
    }

    /*
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNameEntered) {
            mListener = (OnNameEntered) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNameEntered interface");
        }
    }
    */

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnNameEntered {
        // TODO: Update argument type and name
        void onNameEntered (String name, boolean isMale);
    }
}
