package com.example.testmultiphotos;

import android.content.Context;
import android.widget.Toast;

public class ToastHelper {

  private Context context;

  private ToastHelper(Context context) {
    this.context = context;
  }

  public static ToastHelper getInstance(Context context) {
    return new ToastHelper(context);
  }

  public void showLongToast(final String text) {
    Toast.makeText(context, text, Toast.LENGTH_LONG).show();
  }

  public void showLongToast(final int textId, Object... param) {
    String msg = context.getString(textId, param);
    showLongToast(msg);
  }

  public void showShortToast(final String text) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
  }

  public void showShortToast(final int textId, Object... param) {
    String msg = context.getString(textId, param);
    showShortToast(msg);
  }

}
