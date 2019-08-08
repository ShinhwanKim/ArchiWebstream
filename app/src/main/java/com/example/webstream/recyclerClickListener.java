package com.example.webstream;

import android.view.View;

public interface recyclerClickListener {
    void onClick(View view, int position);

    void onLongClick(View view, int position);
}
