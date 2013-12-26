package com.voidstar.glass.autohud;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ObdManager {
	private static final String ADAPTER_NAME = "OBDII";
	
	private final Set<OnChangedListener> listeners;

	private BluetoothAdapter bluetoothAdapter;
	private BluetoothDevice obdDongle;
	private BluetoothSocket sppSocket;
	private BufferedInputStream sppRx;
	private BufferedOutputStream sppTx;

	private static final int NOTHING = 0;
	private static final int TACH = 1;
	private static final int SPEED = 2;
	private static final int FUEL = 4;
	private static final int MAF = 8;
	private static final int CAPABILITIES = 16;
	private static final int PROTOCOL = 32;

	private boolean isAlive = true;
	private int lastRead;
	private StringBuilder activeSentence = new StringBuilder();

	public boolean IsReadyForCommand;
	public List<String> Sentences = new ArrayList<String>();

	private int nextStatToUpdate = NOTHING;

	private int tach;
	private int speed;
	private int fuel;
	private int maf;
	private float mpg;


	public interface OnChangedListener {
		void onTachChanged (ObdManager manager);
		void onSpeedChanged (ObdManager manager);
		void onFuelChanged (ObdManager manager);
		void onMpgChanged (ObdManager manager);
	}


	/**
	 * Adds a listener that will be notified when the user's location or orientation changes.
	 */
	public void addOnChangedListener(OnChangedListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a listener from the list of those that will be notified when the user's location or
	 * orientation changes.
	 */
	public void removeOnChangedListener(OnChangedListener listener) {
		listeners.remove(listener);
	}


	public ObdManager() {
		listeners = new LinkedHashSet<OnChangedListener>();
	}


	public void Connect() {
		class BtConnector implements Runnable {
			public void run() {
				obdDongle = null;
				
				bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
				for (BluetoothDevice device : pairedDevices) {
					Log.i("AutoHud", "Found Paired Device" + device.getName());
					if (device.getName().equals(ADAPTER_NAME)) {
						Log.i("AutoHud", "That's our man");
						obdDongle = device;
					}
				}
				
				if (obdDongle == null) {
					Log.d("VSQuest", "OBD Dongle not found. Check if it's paired, in range, and that the ADAPTER_NAME constant in ObdManager.java is correct.");
					return;
				}

				try {
					sppSocket = obdDongle.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")); // Magic Number UUID for SPP
				}
				catch (Exception e){
					//Log.e("AutoHud", "Couldn't create RFCOMM socket. Sucks to be you");
					e.printStackTrace();
					return;
				}

				Log.i("AutoHud", "Connecting to SPP...");

				try {
					bluetoothAdapter.cancelDiscovery();
					sppSocket.connect();
				}
				catch (IOException e) {
					//Log.e("AutoHud", "Failed to connect - IOException");
					e.printStackTrace();
					return;
				}

				Log.i("AutoHud", "Connected..?");

				try {
					sppRx = new BufferedInputStream(sppSocket.getInputStream());
					sppTx = new BufferedOutputStream(sppSocket.getOutputStream());
				}
				catch (IOException e) {
					//Log.e("AutoHud", "Failed to open Rx and/or Tx");
					e.printStackTrace();
					return;
				}
				
				generateObdCommunicator();
			}
		}
		
		new Thread(new BtConnector()).start();
	}
	
	private void generateObdCommunicator() {
		ObdCommunicator communicator = new ObdCommunicator();
		new Thread(communicator).start();
	}

	public void Disconnect() {
		try {
			Log.d("AutoHud", "Preparing to disconnect. isAlive == false");
			isAlive = false;
			
			if (sppRx != null) {
				sppRx.close();
				Log.d("AutoHud", "Closed SPP RX");
			}
			
			if (sppTx != null) {
				sppTx.close();
				Log.d("AutoHud", "Closed SPP TX");
			}
			
			if (sppSocket != null) {
				sppSocket.close();
				Log.d("AutoHud", "Closed SPP socket");
			}
		}
		catch (IOException e) {
			//Log.e("AutoHud", "Closing sockets failed");
			e.printStackTrace();
		}

		Log.i("AutoHud", "Closed sockets");
	}    	
	
	
	private void notifyTachChanged() {
		for (OnChangedListener listener : listeners) listener.onTachChanged(this);
	}
	
	
	private void notifySpeedChanged() {
		for (OnChangedListener listener : listeners) listener.onSpeedChanged(this);
	}
	
	
	private void notifyFuelChanged() {
		for (OnChangedListener listener : listeners) listener.onFuelChanged(this);
	}
	
	
	private void notifyMpgChanged() {
		for (OnChangedListener listener : listeners) listener.onMpgChanged(this);
	}
	
	
	public int getTach() {
		return tach;
	}
	
	
	public int getSpeed() {
		return speed;
	}
	
	
	public int getFuel() {
		return fuel;
	}
	
	
	public float getMpg() {
		return mpg;
	}


	public class ObdCommunicator implements Runnable {
		public void run() {
			Log.i("AutoHud", "Started new ObdCommunicator.");

			println("AT Z");

			nextStatToUpdate = PROTOCOL;

			while(isAlive) {
				try {
					lastRead = sppRx.read();
				}
				catch (IOException e) {
					//Log.e("AutoHud", "IOException while reading from RX buffer");
					//Log.e("AutoHud", e.getLocalizedMessage());
					e.printStackTrace();
				}

				if (lastRead == -1) continue;

				char lastChar = (char)lastRead;

				IsReadyForCommand = false;

				if (lastChar == '\r') {
					if (activeSentence.length() == 0) continue;

					Sentences.add(activeSentence.toString());

					Log.i("AutoHud", "Adding new sentence to Sentences. Sentence is:");
					Log.i("AutoHud", activeSentence.toString());

					activeSentence.delete(0, activeSentence.length() + 1);
				}
				else if (lastChar == '\n' || lastChar == 0x00) {
					continue;
				}
				else if (lastChar == '>') {
					Log.i("AutoHud", "Received >, Ready for new command");
					IsReadyForCommand = true;
				}
				else {
					Log.d("AutoHud", "Rx Char: " + lastChar);
					activeSentence.append(lastChar);
				}

				if (IsReadyForCommand) {
					if (nextStatToUpdate == TACH) {
						println("01 0C");
						IsReadyForCommand = false;
					}
					else if (nextStatToUpdate == SPEED) {
						println("01 0D");
						IsReadyForCommand = false;
					}
					else if (nextStatToUpdate == FUEL) {
						println("01 2F");
						IsReadyForCommand = false;
					}
					else if (nextStatToUpdate == MAF) {
						println("01 10");
						IsReadyForCommand = false;
					}
					else if (nextStatToUpdate == CAPABILITIES) {
						println("01 00");
						IsReadyForCommand = false;
						// Wait for response to this before firing other commands - indicates OBD is online
					}
					else if (nextStatToUpdate == PROTOCOL) {
						println("AT SP 0"); 
						IsReadyForCommand = false;
						nextStatToUpdate = CAPABILITIES;
					}
				}

				if (!Sentences.isEmpty()) {
					String[] words = Sentences.get(0).split("\\s+");

					if (words[0].equals("41")) { // Response to an OBD request beginning 01
						if (words[1].equals("00")) { // CAPABILITIES
							nextStatToUpdate = TACH;
						}
						else if (words[1].equals("0C")) {
							//tach
							int value = Integer.parseInt(words[2], 16);
							value = (value << 8) & 0xFF00;
							value += Integer.parseInt(words[3], 16);
							value /= 4;

							tach = value;
							notifyTachChanged();
							nextStatToUpdate = SPEED;
						}
						else if (words[1].equals("0D")) {
							//speed
							int value = Integer.parseInt(words[2], 16);
							value = (int)((float)value * 0.6214);

							speed = value;
							notifySpeedChanged();
							nextStatToUpdate = FUEL;
						}
						else if (words[1].equals("2F")) {
							// fuel
							int value = Integer.parseInt(words[2], 16);
							value *= 100;
							value /= 255;

							fuel = value;
							notifyFuelChanged();
							nextStatToUpdate = MAF;
						}
						else if (words[1].equals("10")) {
							// maf
							int value = Integer.parseInt(words[2], 16);
							value = (value << 8) & 0xFF00;
							value += Integer.parseInt(words[3], 16);
							//value /= 100;

							maf = value;
							if (maf > 0 && speed > 0) mpg = ((float)speed / (float)maf) * 710.7f; // Calculation from Circuit Cellar 
							else mpg = 0;
							notifyMpgChanged();
							nextStatToUpdate = TACH;
						}
					}

					Sentences.remove(0);
				}
			}
		}
		
		
		private void println(String command) {
			try {
				Log.i("AutoHud", "Printing command " + command);
				sppTx.write(command.getBytes());
				sppTx.write('\r');
				sppTx.flush();
			}
			catch (IOException e) { e.printStackTrace(); /*Log.e("AutoHud", e.getLocalizedMessage());*/ }
		}
	}

}
