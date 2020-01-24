package com.example.webstream;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class ArActivity extends AppCompatActivity {
    private static final String TAG = ArActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;
    public void SetLog(String content){
        Log.e("TAG",content);
    }

    private ArFragment arFragment;
    private ModelRenderable bedRenderable,bookcaseRenderable,deskRenderable,dishwasherRenderable,pianoRenderable;

    ImageView bed,bookcase,desk,dishwasher,piano;

    int selected = 1;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_ar);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
        ModelRenderable.builder()
                .setSource(this, R.raw.bed)
                .build()
                .thenAccept(renderable -> bedRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
        ModelRenderable.builder()
                .setSource(this, R.raw.bookcase)
                .build()
                .thenAccept(renderable -> bookcaseRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
        ModelRenderable.builder()
                .setSource(this, R.raw.desk)
                .build()
                .thenAccept(renderable -> deskRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
        ModelRenderable.builder()
                .setSource(this, R.raw.dishwasher)
                .build()
                .thenAccept(renderable -> dishwasherRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
        ModelRenderable.builder()
                .setSource(this, R.raw.piano)
                .build()
                .thenAccept(renderable -> pianoRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
//          if (andyRenderable == null) {
//            return;
//          }
                    // Create the Anchor.
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());
                    switch (selected){
                        case 1:

                            // Create the transformable andy and add it to the anchor.
                            TransformableNode bedNode = new TransformableNode(arFragment.getTransformationSystem());
                            bedNode.setParent(anchorNode);
                            bedNode.setRenderable(bedRenderable);
                            bedNode.select();
                            break;
                        case 2:
                            // Create the transformable andy and add it to the anchor.
                            TransformableNode bookcaseNode = new TransformableNode(arFragment.getTransformationSystem());
                            bookcaseNode.setParent(anchorNode);
                            bookcaseNode.setRenderable(bookcaseRenderable);
                            bookcaseNode.select();
                            break;
                        case 3:
                            // Create the transformable andy and add it to the anchor.
                            TransformableNode deskNode = new TransformableNode(arFragment.getTransformationSystem());
                            deskNode.setParent(anchorNode);
                            deskNode.setRenderable(deskRenderable);
                            deskNode.select();
                            break;
                        case 4:
                            // Create the transformable andy and add it to the anchor.
                            TransformableNode dishwasherNode = new TransformableNode(arFragment.getTransformationSystem());
                            dishwasherNode.setParent(anchorNode);
                            dishwasherNode.setRenderable(dishwasherRenderable);
                            dishwasherNode.select();
                            break;
                        case 5:
                            // Create the transformable andy and add it to the anchor.
                            TransformableNode pianoNode = new TransformableNode(arFragment.getTransformationSystem());
                            pianoNode.setParent(anchorNode);
                            pianoNode.setRenderable(pianoRenderable);
                            pianoNode.select();
                            break;
                    }

                });
        bed = findViewById(R.id.bed);
        bookcase = findViewById(R.id.bookcase);
        desk = findViewById(R.id.desk);
        dishwasher = findViewById(R.id.dishwasher);
        piano = findViewById(R.id.piano);

        bed.setBackgroundResource(R.color.colorBluePoint);

        bed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetLog("침대");
                selected = 1;
                bed.setBackgroundResource(R.color.colorBluePoint);
                bookcase.setBackgroundResource(R.color.trans);
                desk.setBackgroundResource(R.color.trans);
                dishwasher.setBackgroundResource(R.color.trans);
                piano.setBackgroundResource(R.color.trans);

            }
        });
        bookcase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetLog("책장");
                selected = 2;
                bed.setBackgroundResource(R.color.trans);
                bookcase.setBackgroundResource(R.color.colorBluePoint);
                desk.setBackgroundResource(R.color.trans);
                dishwasher.setBackgroundResource(R.color.trans);
                piano.setBackgroundResource(R.color.trans);
            }
        });
        desk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetLog("책상");
                selected = 3;
                bed.setBackgroundResource(R.color.trans);
                bookcase.setBackgroundResource(R.color.trans);
                desk.setBackgroundResource(R.color.colorBluePoint);
                dishwasher.setBackgroundResource(R.color.trans);
                piano.setBackgroundResource(R.color.trans);
            }
        });
        dishwasher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetLog("식기");
                selected = 4;
                bed.setBackgroundResource(R.color.trans);
                bookcase.setBackgroundResource(R.color.trans);
                desk.setBackgroundResource(R.color.trans);
                dishwasher.setBackgroundResource(R.color.colorBluePoint);
                piano.setBackgroundResource(R.color.trans);
            }
        });
        piano.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetLog("피아노");
                selected = 5;
                bed.setBackgroundResource(R.color.trans);
                bookcase.setBackgroundResource(R.color.trans);
                desk.setBackgroundResource(R.color.trans);
                dishwasher.setBackgroundResource(R.color.trans);
                piano.setBackgroundResource(R.color.colorBluePoint);
            }
        });
    }

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * <p>Finishes the activity if Sceneform can not run
     */
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }
}
