package com.example.tazo.myapplicationwatch;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener,
        NodeApi.NodeListener,
        MessageApi.MessageListener,
        DataApi.DataListener{

    private TextView mTextView;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        //Toast.makeText(this,"hi",Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!googleApiClient.isConnected()){
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(this,"들어왔따",Toast.LENGTH_SHORT).show();
        Wearable.NodeApi.addListener(googleApiClient,this);
        Wearable.MessageApi.addListener(googleApiClient,this);
        Wearable.DataApi.addListener(googleApiClient,this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this,"onConnectionSuspended",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this,"fail",Toast.LENGTH_SHORT).show();
        Wearable.NodeApi.removeListener(googleApiClient,this);
        Wearable.MessageApi.removeListener(googleApiClient,this);
        Wearable.DataApi.removeListener(googleApiClient,this);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        for(DataEvent event:dataEventBuffer){
            if(event.getType() == DataEvent.TYPE_CHANGED){

                String path = event.getDataItem().getUri().getPath();

                if(path.equals("/KNOW_VIBE_PATH")){
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    long[] patter_ms = dataMapItem.getDataMap().getLongArray("know");

                    vibrator.vibrate(patter_ms,-1);
                }

                if(path.equals("/DONT_VIBE_PATH")){
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    long[] patter_ms = dataMapItem.getDataMap().getLongArray("dontknow");

                    vibrator.vibrate(patter_ms,-1);
                }

                if(path.equals("/GIVE_ALARM")){
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    long[] take_alarm = dataMapItem.getDataMap().getLongArray("alarm");
                    Toast.makeText(this,"알람 울리는 중",Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(take_alarm,0);
                }
                if(path.equals("/STOP_ALARM")){
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    vibrator.cancel();
                }

            }
        }
    }
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
    }

    @Override
    public void onPeerConnected(Node node) {
        Toast.makeText(this,"peer connect",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPeerDisconnected(Node node) {
        Toast.makeText(this,"peer dis",Toast.LENGTH_SHORT).show();
    }

    public void OnClickModeButton(View v) {

    }

}
