package org.mukdongjeil.mjchurch.board;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.util.Logger;

/**
 * Created by John Kim on 2016-11-12.
 */
public class BoardWriteFragment extends Fragment {
    private static final String TAG = BoardWriteFragment.class.getSimpleName();

    private EditText edtWriter;
    private EditText edtContent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.i(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_write_board, container, false);
        edtWriter = (EditText) v.findViewById(R.id.edt_writer);
        edtContent = (EditText) v.findViewById(R.id.edt_content);
        v.findViewById(R.id.btn_write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.e(TAG, "writer : " + edtWriter.getText().toString() + ", content : " + edtContent.getText().toString());
                Logger.e(TAG, "write done btn clicked");
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle("감사나눔 글쓰기");
    }
}
