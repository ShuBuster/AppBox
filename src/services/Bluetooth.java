package services;

/*
 * Projet SIT, @copyright 2015 SAGEM DS
 *
 * Les informations contenues dans ce fichier sont la propri�t� de
 * SAGEM DS et diffus�es � titre confidentiel dans un but sp�cifique.
 * Le destinataire assure la garde et la surveillance de ce fichier et
 * convient qu'il ne sera ni copi� ni reproduit en tout ou partie et
 * que son contenu ne sera r�v�l� en aucune mani�re � aucune personne,
 * except� pour r�pondre au but pour le quel il a �t� transmis.
 * Cette recommandation est applicable � tous les documents g�n�r�s �
 * partir de ce fichier.
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * @author local
 */
public class Bluetooth {

	// Debugging
	private static final String TAG = "BluetoothService";

	private static final int TAILLE_BUFFER = 1024 * 32;

	// Name for the SDP record when creating server socket
	private static final String NAME_SECURE = "BluetoothSecure";

	private static final String NAME_INSECURE = "BluetoothInsecure";

	// Unique UUID for this application
	private static final UUID MY_UUID_SECURE = UUID
			.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

	private static final UUID MY_UUID_INSECURE = UUID
			.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

	// Constants that indicate the current connection state

	/* We're doing nothing. */
	public static final int STATE_NONE = 0;

	/* now listening for incoming connections */
	public static final int STATE_LISTEN = 1;

	/* now initiating an outgoing connection */
	public static final int STATE_CONNECTING = 2;

	/* now connected to a remote device */
	public static final int STATE_CONNECTED = 3;

	// Member fields
	/** The bluetooth adapter is the basis of bluetooth connection. */
	private final BluetoothAdapter mAdapter;

	/** A Handler to send messages back to the UI Activity. */
	private Handler mHandler;

	/** The secure server side thread, listens for conections. */
	private AcceptThread mSecureAcceptThread;

	/** The insecure server side thread, listens for connections. */
	private AcceptThread mInsecureAcceptThread;

	/** The client side init a connection. */
	private ConnectThread mConnectThread;

	/**
	 * The connected thread performs wrinting and reading when a connection is
	 * established.
	 */
	private ConnectedThread mConnectedThread;

	/** The actual state of a connection. */
	private int mState;

	/**
	 * Constructor. Prepares a new Bluetooth session.
	 * 
	 * @param context
	 *            The UI Activity Context
	 * @param handler
	 *            A Handler to send messages back to the UI Activity
	 */
	public Bluetooth(final Context context, final Handler handler) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		mHandler = handler;
	}

	/**
	 * Set the current state of the connection
	 * 
	 * @param state
	 *            An integer defining the current connection state
	 */
	private synchronized void setState(final int state) {
		Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;

		// Give the new state to the Handler so the UI Activity can update
		mHandler.obtainMessage(Bluetooth_Constants.MESSAGE_STATE_CHANGE, state,
				-1).sendToTarget();
	}

	/**
	 * l'etat de la connexion en cours.
	 */
	public synchronized int getState() {
		return mState;
	}

	/**
     *
     */
	public void setHandler(final Handler handler) {
		mHandler = handler;
	}

	/**
	 * Start the service. Specifically start AcceptThread to begin a session in
	 * listening (server) mode. Called by the Activity onResume()
	 */
	public synchronized void start() {
		Log.d(TAG, "start");

		// Cancel any thread attempting to make a connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		setState(STATE_LISTEN);

		// Start the thread to listen on a BluetoothServerSocket
		if (mSecureAcceptThread == null) {
			mSecureAcceptThread = new AcceptThread(true);
			mSecureAcceptThread.start();
		}
		if (mInsecureAcceptThread == null) {
			mInsecureAcceptThread = new AcceptThread(false);
			mInsecureAcceptThread.start();
		}
	}

	/**
	 * Start the ConnectThread (client) to initiate a connection to a remote
	 * device.
	 * 
	 * @param device
	 *            The BluetoothDevice to connect
	 * @param secure
	 *            Socket Security type - Secure (true) , Insecure (false) device
	 *            is null in case of network connection or if bluetooth is not
	 *            enabled on the Smartphone.
	 */
	public synchronized void connect(final BluetoothDevice device,
			final boolean secure) {
		if (device != null) {
			Log.d(TAG, "connect to: " + device);

			// Cancel any thread attempting to make a connection
			if (mState == STATE_CONNECTING) {
				if (mConnectThread != null) {
					mConnectThread.cancel();
					mConnectThread = null;
				}
			}

			// Cancel any thread currently running a connection
			if (mConnectedThread != null) {
				mConnectedThread.cancel();
				mConnectedThread = null;
			}

			// Start the thread to connect with the given device
			mConnectThread = new ConnectThread(device, secure);
			mConnectThread.start();
			setState(STATE_CONNECTING);
		}
	}

	/**
	 * Start the ConnectedThread (input and output streams are managed here) to
	 * begin managing a Bluetooth connection
	 * 
	 * @param socket
	 *            The BluetoothSocket on which the connection was made
	 * @param device
	 *            The BluetoothDevice that has been connected
	 */
	private synchronized void connected(final BluetoothSocket socket,
			final BluetoothDevice device, final String socketType) {
		Log.d(TAG, "connected, Socket Type:" + socketType);

		// Cancel the thread that completed the connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Cancel the accept thread because we only want to connect to one
		// device
		if (mSecureAcceptThread != null) {
			mSecureAcceptThread.cancel();
			mSecureAcceptThread = null;
		}
		if (mInsecureAcceptThread != null) {
			mInsecureAcceptThread.cancel();
			mInsecureAcceptThread = null;
		}

		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(socket, socketType);
		mConnectedThread.start();

		// Send the name of the connected device back to the UI Activity
		final Message msg = mHandler
				.obtainMessage(Bluetooth_Constants.MESSAGE_DEVICE_NAME);
		final Bundle bundle = new Bundle();
		bundle.putString(Bluetooth_Constants.DEVICE_NAME, device.getName());
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		setState(STATE_CONNECTED);
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		Log.d(TAG, "stop");

		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		if (mSecureAcceptThread != null) {
			mSecureAcceptThread.cancel();
			mSecureAcceptThread = null;
		}

		if (mInsecureAcceptThread != null) {
			mInsecureAcceptThread.cancel();
			mInsecureAcceptThread = null;
		}
		setState(STATE_NONE);
	}

	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * 
	 * @param mes
	 *            l'emploi du temps a ecrire
	 * @see ConnectedThread#write(WatchMessage)
	 */
	public void write(final byte[] mes) {
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			if (mState != STATE_CONNECTED) {
				return;
			}
			r = mConnectedThread;
		}
		// Perform the write unsynchronized
		r.write(mes);
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed() {
		// Send a failure message back to the Activity
		final Message msg = mHandler
				.obtainMessage(Bluetooth_Constants.MESSAGE_TOAST);
		final Bundle bundle = new Bundle();
		bundle.putString(Bluetooth_Constants.TOAST,
				"Impossible de se connecter a un appareil");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
		setState(STATE_NONE);
		// Start the service over to restart listening mode
		// Bluetooth.this.start();
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		// Send a failure message back to the Activity
		final Message msg = mHandler
				.obtainMessage(Bluetooth_Constants.MESSAGE_TOAST);
		final Bundle bundle = new Bundle();
		bundle.putString(Bluetooth_Constants.TOAST,
				"La connexion avec l'appareil a ete perdue");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
		setState(STATE_NONE);
		// Start the service over to restart listening mode
		// Bluetooth.this.start();
	}

	/**
	 * This thread runs while listening for incoming connections. It behaves
	 * like a server-side client. It runs until a connection is accepted (or
	 * until cancelled).
	 */
	private class AcceptThread extends Thread {

		// The local server socket
		private final BluetoothServerSocket mmServerSocket;

		private final String mSocketType;

		public AcceptThread(final boolean secure) {
			BluetoothServerSocket tmp = null;
			mSocketType = secure ? "Secure" : "Insecure";

			// Create a new listening server socket
			try {
				if (secure) {
					tmp = mAdapter.listenUsingRfcommWithServiceRecord(
							NAME_SECURE, MY_UUID_SECURE);
				} else {
					tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
							NAME_INSECURE, MY_UUID_INSECURE);
				}
			} catch (final IOException e) {
				Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
			}
			mmServerSocket = tmp;
		}

		@Override
		public void run() {
			Log.d(TAG, "Socket Type: " + mSocketType + "BEGIN mAcceptThread"
					+ this);
			setName("AcceptThread" + mSocketType);

			BluetoothSocket socket = null;

			// Listen to the server socket if we're not connected
			while (mState != STATE_CONNECTED) {
				try {
					// This is a blocking call and will only return on a
					// successful connection or an exception
					socket = mmServerSocket.accept();
				} catch (final IOException e) {
					Log.e(TAG, "Socket Type: " + mSocketType
							+ "accept() failed", e);
					break;
				}

				// If a connection was accepted
				if (socket != null) {
					synchronized (Bluetooth.this) {
						switch (mState) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							// Situation normal. Start the connected thread.
							connected(socket, socket.getRemoteDevice(),
									mSocketType);
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							// Either not ready or already connected. Terminate
							// new socket.
							try {
								socket.close();
							} catch (final IOException e) {
								Log.e(TAG, "Could not close unwanted socket", e);
							}
							break;
						}
					}
				}
			}
			Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

		}

		public void cancel() {
			Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
			try {
				mmServerSocket.close();
			} catch (final IOException e) {
				Log.e(TAG, "Socket Type" + mSocketType
						+ "close() of server failed", e);
			}
		}
	}

	/**
	 * This thread runs while attempting to make an outgoing connection with a
	 * device. It runs straight through; the connection either succeeds or
	 * fails.
	 */
	private class ConnectThread extends Thread {

		private final BluetoothSocket mmSocket;

		private final BluetoothDevice mmDevice;

		private final String mSocketType;

		public ConnectThread(final BluetoothDevice device, final boolean secure) {
			mmDevice = device;
			BluetoothSocket tmp = null;
			mSocketType = secure ? "Secure" : "Insecure";

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				if (secure) {
					tmp = device
							.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
				} else {
					tmp = device
							.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
				}
			} catch (final IOException e) {
				Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
			}
			mmSocket = tmp;
		}

		@Override
		public void run() {
			Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
			setName("ConnectThread" + mSocketType);

			// Always cancel discovery because it will slow down a connection
			mAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mmSocket.connect();
			} catch (final IOException e) {
				// Close the socket
				try {
					mmSocket.close();
				} catch (final IOException e2) {
					Log.e(TAG, "unable to close() " + mSocketType
							+ " socket during connection failure", e2);
				}
				connectionFailed();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (Bluetooth.this) {
				mConnectThread = null;
			}

			// Start the connected thread
			connected(mmSocket, mmDevice, mSocketType);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (final IOException e) {
				Log.e(TAG, "close() of connect " + mSocketType
						+ " socket failed", e);
			}
		}
	}

	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {

		private final BluetoothSocket mmSocket;

		private final InputStream mmInStream;

		private final OutputStream mmOutStream;

		public ConnectedThread(final BluetoothSocket socket,
				final String socketType) {
			Log.d(TAG, "create ConnectedThread: " + socketType);
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (final IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		@Override
		public void run() {
			Log.i(TAG, "BEGIN mConnectedThread");
			final byte[] buffer = new byte[TAILLE_BUFFER];
			int bytes;

			// Keep listening to the InputStream while connected
			while (true) {
				try {

					if (mmInStream.available() > 0) {
						// Read from the InputStream
						bytes = mmInStream.read(buffer);
						// Send the obtained bytes to the UI Activity
						mHandler.obtainMessage(
								Bluetooth_Constants.MESSAGE_READ, bytes, -1,
								buffer).sendToTarget();
					} else
						SystemClock.sleep(100);
				} catch (final IOException e) {
					Log.e(TAG, "disconnected", e);
					connectionLost();
					break;
				}

			}
		}

		/**
		 * Write to the connected OutStream.
		 * 
		 * @param buffer
		 *            The WatchMessage to write
		 */
		private void write(final byte[] buffer) {
			try {
				mmOutStream.write(buffer);
				mmOutStream.flush();

				// Share the sent message back to the UI Activity
				mHandler.obtainMessage(Bluetooth_Constants.MESSAGE_WRITE,
						buffer.length, -1, buffer).sendToTarget();
			} catch (final IOException e) {
				Log.e(TAG, "Exception during write", e);
			}

		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (final IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}
}
