package com.bbbbiu.biu.lib.servlet.apple;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.bbbbiu.biu.lib.httpd.ContentType;
import com.bbbbiu.biu.lib.httpd.HttpDaemon;
import com.bbbbiu.biu.lib.httpd.HttpRequest;
import com.bbbbiu.biu.lib.httpd.HttpResponse;
import com.bbbbiu.biu.lib.httpd.HttpServlet;
import com.bbbbiu.biu.util.StorageUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by YieldNull at 6/5/16
 */
public class IconServlet extends HttpServlet {
    private static final String TAG = IconServlet.class.getSimpleName();

    public static void register(Context context) {
        HttpDaemon.registerServlet("^/icon/((?!/).)+$", new IconServlet(context));
    }

    private IconServlet(Context context) {
        super(context);
    }

    @Override
    public HttpResponse doGet(HttpRequest request) {
        String uri = request.getUri();

        Drawable drawable = StorageUtil.getFileIcon(context, new File(uri));

        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        Bitmap bitmap = bitmapDrawable.getBitmap();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] imageInByte = stream.toByteArray();

        return HttpResponse.newResponse(ContentType.MIME_PNG,
                new ByteArrayInputStream(imageInByte), imageInByte.length);
    }

    @Override
    public HttpResponse doPost(HttpRequest request) {
        return null;
    }
}