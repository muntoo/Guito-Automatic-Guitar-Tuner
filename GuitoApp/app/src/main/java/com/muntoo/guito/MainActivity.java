package com.muntoo.guito;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

//import be.tarsos.dsp.*;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;


public class MainActivity extends Activity
{
	private static final String TAG = "MainActivity";
	private static final String DEVICE_ADDRESS = "98:D3:31:60:05:C7"; // change this to your Bluetooth device address
	private ArduinoReceiver arduinoReceiver = new ArduinoReceiver();
	private Context context = this; // getApplicationContext();
	// private recorderThread audioThread = new recorderThread();
	// private MicrophoneThread audioThread = new MicrophoneThread();
	// private Thread pitch_detector_thread_ = null;



	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// get handles to Views defined in our layout file
		final TextView numCurrentPitch = (TextView) findViewById(R.id.numCurrentPitch);
		Button btnTuneStandard = (Button) findViewById(R.id.btnTuneStandard);

		// audioThread.start();
		// microphoneThread.start();




		AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);


		dispatcher.addAudioProcessor(new PitchProcessor(PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, new PitchDetectionHandler() {

			@Override
			public void handlePitch(PitchDetectionResult pitchDetectionResult,
			                        AudioEvent audioEvent) {
				final float pitchInHz = pitchDetectionResult.getPitch();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						TextView numCurrentPitch = (TextView) findViewById(R.id.numCurrentPitch);
						numCurrentPitch.setText(pitchInHz + "");
						//TextView text = (TextView) findViewById(R.id.textView1);
						//text.setText("" + pitchInHz);
					}
				});

			}
		}));


		new Thread(dispatcher,"Audio Dispatcher").start();


		btnTuneStandard.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				//numCurrentPitch.setText(audioThread.frequency + "");
                //Amarino.sendDataToArduino(context, DEVICE_ADDRESS, 'A', audioThread.frequency);
			}
		});
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		// in order to receive broadcasted intents we need to register our receiver
		registerReceiver(arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));

		// this is how you tell Amarino to connect to a specific BT device from within your own code
		Amarino.connect(context, DEVICE_ADDRESS);


		//pitch_detector_thread_ = new Thread(new PitchDetector(this, new Handler()));
		//pitch_detector_thread_.start();
	}

	@Override
	protected void onStop() {
		super.onStop();

		// if you connect in onStart() you must not forget to disconnect when your app is closed
		Amarino.disconnect(context, DEVICE_ADDRESS);

		// do never forget to unregister a registered receiver
		unregisterReceiver(arduinoReceiver);
	}



	/**
	 * ArduinoReceiver is responsible for catching broadcasted Amarino
	 * events.
	 * <p/>
	 * It extracts data from the intent and updates the graph accordingly.
	 */
	public class ArduinoReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			// Amarino.sendDataToArduino(context, DEVICE_ADDRESS, 'A', "test");

			// the device address from which the data was sent, we don't need it here but to demonstrate how you retrieve it
			final String address = intent.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);

			// the type of data which is added to the intent
			final int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);

			// we only expect String data though, but it is better to check if really string was sent
			// later Amarino will support different data types, so far data comes always as string and
			// you have to parse the data to the type you have sent from Arduino, like it is shown below
			if (dataType == AmarinoIntent.STRING_EXTRA)
			{
				String data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);

				if (data != null)
				{
					TextView numCurrentPitch = (TextView) findViewById(R.id.numCurrentPitch);
                    // numCurrentPitch.setText(data);
				}
			}
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
