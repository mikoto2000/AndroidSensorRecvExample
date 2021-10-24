package dev.mikoto2000.android.sensorrecv;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * センサー情報取得クラス
 *
 * See: https://taiyoproject.com/post-118
 *
 */
public class OrientationListener implements SensorEventListener {
    /** デバッグ用 */
    private static final boolean DEBUG = true;
    private static final String TAG = "OrientationListener";
    /** 行列数 */
    private static final int MATRIX_SIZE = 16;
    /** 三次元(XYZ) */
    private static final int DIMENSION = 3;
    /** センサー管理クラス */
    private SensorManager mManager;
    /** 地磁気行列 */
    private float[] mMagneticValues;
    /** 加速度行列 */
    private float[] mAccelerometerValues;
    /** 加速度(重力加速度抜き)行列 */
    private float[] mLinearAccelerometerValues = new float[]{0,0,0};
    /** X軸の回転角度 */
    private int mPitchX;
    /** Y軸の回転角度 */
    private int mRollY;
    /** Z軸の回転角度(方位角) */
    private int mAzimuthZ;

    /**
     * センサーイベント取得開始
     *
     * @param context
     *            コンテキスト
     */
    public synchronized void resume(Context context) {
        if (context == null) {
            // 引数不正
            return;
        }
        // 登録済なら一旦止める
        pause();
        if (mManager == null) {
            // 初回実行時
            mManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        // 地磁気センサー登録
        mManager.registerListener(this, mManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
        // 加速度センサー登録
        mManager.registerListener(this, mManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        // 加速度センサー登録
        mManager.registerListener(this, mManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_UI);
    }

    /**
     * センサーイベント取得終了
     */
    public synchronized void pause() {
        if (mManager != null) {
            mManager.unregisterListener(this);
        }
    }

    /**
     * Z軸の回転角度(方位角)を取得する
     *
     * @return Z軸の回転角度
     */
    public synchronized int getAzimuth() {
        return mAzimuthZ;
    }

    public synchronized float getAccelX() {
        return mLinearAccelerometerValues[0];
    }

    public synchronized float getAccelY() {
        return mLinearAccelerometerValues[1];
    }

    public synchronized float getAccelZ() {
        return mLinearAccelerometerValues[2];
    }

    /**
     * ラジアンを角度に変換する
     *
     * @param angrad
     *            ラジアン
     * @return 角度
     */
    private int radianToDegrees(float angrad) {
        return (int) Math.floor(angrad >= 0 ? Math.toDegrees(angrad) : 360 + Math.toDegrees(angrad));
    }

    @Override
    public synchronized void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 処理なし
    }

    @Override
    public synchronized void onSensorChanged(SensorEvent event) {
        // センサーイベント
        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                // 地磁気センサー
                mMagneticValues = event.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                // 加速度センサー
                mAccelerometerValues = event.values.clone();
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                // 加速度センサー(重力加速度抜き)
                mLinearAccelerometerValues = event.values.clone();
                break;
            default:
                // それ以外は無視
                return;
        }
        if (mMagneticValues != null && mAccelerometerValues != null) {
            float[] rotationMatrix = new float[MATRIX_SIZE];
            float[] inclinationMatrix = new float[MATRIX_SIZE];
            float[] remapedMatrix = new float[MATRIX_SIZE];
            float[] orientationValues = new float[DIMENSION];
            // 加速度センサーと地磁気センサーから回転行列を取得
            SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, mAccelerometerValues, mMagneticValues);
            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remapedMatrix);
            SensorManager.getOrientation(remapedMatrix, orientationValues);
            // ラジアン値を変換し、それぞれの回転角度を取得する
            mAzimuthZ = radianToDegrees(orientationValues[0]);
            mPitchX = radianToDegrees(orientationValues[1]);
            mRollY = radianToDegrees(orientationValues[2]);
            if (DEBUG) {
                Log.d(TAG, "X=" + mPitchX + "Y=" + mRollY + "Z=" + mAzimuthZ);
            }
        }
    }
}