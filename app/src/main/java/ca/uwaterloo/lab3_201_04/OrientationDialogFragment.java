package ca.uwaterloo.lab3_201_04;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.v4.app.DialogFragment;
import android.view.View.*;

public class OrientationDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        return new AlertDialog.Builder(getActivity())
                .setMessage("Please calibrate your phone by rotating it along each axis twice.")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        accValues.stepCheckEnabled = true;
                        dialog.dismiss();
                    }
                })
                .create();
    }

}
