package com.google.android.apps.lightcycle.util;

import java.io.InputStream;

public abstract interface InputStreamFactory
{
  public abstract InputStream create();
}