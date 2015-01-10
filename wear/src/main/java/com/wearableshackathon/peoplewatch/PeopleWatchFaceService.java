package com.wearableshackathon.peoplewatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;

import java.util.concurrent.TimeUnit;

/**
 * author: ericj
 * Creates new watchface showing locations.
 */
public class PeopleWatchFaceService extends CanvasWatchFaceService {
    private static final String TAG = "PeopleWatchFaceService";

    /**
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    @Override
    public Engine onCreateEngine() {
        /* provide your watch face implementation */
        return new Engine();
    }

    /* implement service callback methods */
    private class Engine extends CanvasWatchFaceService.Engine {
        static final int MSG_UPDATE_TIME = 0;

        boolean mMute;

        /** Handler to update the time once a second in interactive mode. */
        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        if (Log.isLoggable(TAG, Log.VERBOSE)) {
                            Log.v(TAG, "updating time");
                        }
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = INTERACTIVE_UPDATE_RATE_MS
                                    - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };

        final BroadcastReceiver mLocationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("message");
                Log.i("mLocationReceiver", message);

            }
        };
        boolean mRegisteredLocationReceiver = false;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        /* graphic objects */
        Bitmap mBackgroundBitmap;
        Bitmap mBackgroundScaledBitmap;

        Paint mRedHand;
        Paint mBlueHand;
        Paint mYellowHand;
        Paint mGreenHand;

        float[] fixedPositions = {
                0f,
                (45/180f),
                (90/180f),
                (135/180f),
                (180/180f),
                (225/180f),
                (270/180f),
                (315/180f)
        };

        int[][] userPositions = {
                {0, 0},
                {1, 1},
                {2, 2},
                {3, 3}
        };
        //

        @Override
        public void onCreate(SurfaceHolder holder) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onCreate");
            }
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(PeopleWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setStatusBarGravity(Gravity.CENTER_VERTICAL)
                    .build());

            Resources resources = PeopleWatchFaceService.this.getResources();
            Drawable backgroundDrawable = resources.getDrawable(R.drawable.roundcon);
            mBackgroundBitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();

            mRedHand = new Paint();
            mRedHand.setARGB(255, 255, 200, 200);
            mRedHand.setStrokeWidth(5.f);
            mRedHand.setAntiAlias(true);
            mRedHand.setStrokeCap(Paint.Cap.ROUND);

            mBlueHand = new Paint();
            mBlueHand.setARGB(255, 0, 0, 255);
            mBlueHand.setStrokeWidth(5.f);
            mBlueHand.setAntiAlias(true);
            mBlueHand.setStrokeCap(Paint.Cap.ROUND);

            mYellowHand = new Paint();
            mYellowHand.setARGB(255, 200, 200, 200);
            mYellowHand.setStrokeWidth(5.f);
            mYellowHand.setAntiAlias(true);
            mYellowHand.setStrokeCap(Paint.Cap.ROUND);

            mGreenHand = new Paint();
            mGreenHand.setARGB(255, 0, 255, 0);
            mGreenHand.setStrokeWidth(5.f);
            mGreenHand.setAntiAlias(true);
            mGreenHand.setStrokeCap(Paint.Cap.ROUND);

            IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
            MessageReceiver messageReceiver = new MessageReceiver();
            PeopleWatchFaceService.this.registerReceiver(messageReceiver, messageFilter);
        }

        public class MessageReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("message");
                Log.i("Message", message);
            }
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onPropertiesChanged: low-bit ambient = " + mLowBitAmbient);
            }
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onTimeTick: ambient = " + isInAmbientMode());
            }
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onAmbientModeChanged: " + inAmbientMode);
            }
            if (mLowBitAmbient) {
                boolean antiAlias = !inAmbientMode;
                mGreenHand.setAntiAlias(antiAlias);
                mBlueHand.setAntiAlias(antiAlias);
                mRedHand.setAntiAlias(antiAlias);
                mYellowHand.setAntiAlias(antiAlias);
            }
            invalidate();
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            boolean inMuteMode = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);
            if (mMute != inMuteMode) {
                mMute = inMuteMode;
                mRedHand.setAlpha(inMuteMode ? 100 : 255);
                mGreenHand.setAlpha(inMuteMode ? 100 : 255);
                mBlueHand.setAlpha(inMuteMode ? 80 : 255);
                invalidate();
            }
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {

            int width = bounds.width();
            int height = bounds.height();

            // Draw the background, scaled to fit.
            if (mBackgroundScaledBitmap == null
                    || mBackgroundScaledBitmap.getWidth() != width
                    || mBackgroundScaledBitmap.getHeight() != height) {
                mBackgroundScaledBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                        width, height, true /* filter */);
            }
            canvas.drawBitmap(mBackgroundScaledBitmap, 0, 0, null);

            // Find the center. Ignore the window insets so that, on round watches with a
            // "chin", the watch face is centered on the entire screen, not just the usable
            // portion.

            float centerX = width / 2f;
            float centerY = height / 2f;

            float secLength = centerX - 20;
            float minLength = centerX - 40;
            float hrLength = centerX - 80;

            // Blue 0
            // Green 1
            // Red 2
            // Yellow 3
            int blueUser = userPositions[0][0];
            float secRot = (fixedPositions[blueUser]) * (float) Math.PI;
            float secX = (float) Math.sin(secRot) * secLength;
            float secY = (float) -Math.cos(secRot) * secLength;
            canvas.drawLine(centerX, centerY, centerX + secX, centerY + secY, mBlueHand);


            int greenUser = userPositions[1][0];
            secRot = (fixedPositions[greenUser]) * (float) Math.PI;
            secX = (float) Math.sin(secRot) * minLength;
            secY = (float) -Math.cos(secRot) * minLength;
            canvas.drawLine(centerX, centerY, centerX + secX, centerY + secY, mGreenHand);
//            RectF change = new RectF(centerX, centerY, centerX + minX, centerY + minY);
//            canvas.drawBitmap(mGreenHand, null, change, null);

            int redUser = userPositions[2][0];
            secRot = (fixedPositions[redUser]) * (float) Math.PI;
            secX = (float) Math.sin(secRot) * hrLength;
            secY = (float) -Math.cos(secRot) * hrLength;
            canvas.drawLine(centerX, centerY, centerX + secX, centerY + secY, mRedHand);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onVisibilityChanged: " + visible);
            }

            if (visible) {
                registerReceiver();

            } else {
                unregisterReceiver();
            }

        }

        private void registerReceiver() {
            if (mRegisteredLocationReceiver) {
                return;
            }
            mRegisteredLocationReceiver = true;
            IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
            PeopleWatchFaceService.this.registerReceiver(mLocationReceiver, messageFilter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredLocationReceiver) {
                return;
            }
            mRegisteredLocationReceiver = false;
            PeopleWatchFaceService.this.unregisterReceiver(mLocationReceiver);
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

    }
}
