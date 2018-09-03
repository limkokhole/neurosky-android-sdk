package com.github.pwittchen.neurosky.app.kotlin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import butterknife.ButterKnife
import butterknife.OnClick
import com.github.pwittchen.neurosky.library.NeuroSky
import com.github.pwittchen.neurosky.library.Preconditions
import com.github.pwittchen.neurosky.library.exception.BluetoothNotEnabledException
import com.github.pwittchen.neurosky.library.listener.ExtendedDeviceMessageListener
import com.github.pwittchen.neurosky.library.message.enums.BrainWave
import com.github.pwittchen.neurosky.library.message.enums.Signal
import com.github.pwittchen.neurosky.library.message.enums.State
import kotlinx.android.synthetic.main.activity_main.tv_attention
import kotlinx.android.synthetic.main.activity_main.tv_blink
import kotlinx.android.synthetic.main.activity_main.tv_meditation
import kotlinx.android.synthetic.main.activity_main.tv_state
import java.util.Locale

class MainActivity : AppCompatActivity() {

  companion object {
    const val LOG_TAG = "NeuroSky"
  }

  private lateinit var neuroSky: NeuroSky

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    ButterKnife.bind(this)
    neuroSky = createNeuroSky()
  }

  override fun onResume() {
    super.onResume()
    if (Preconditions.isConnected(neuroSky.device)) {
      neuroSky.startMonitoring()
    }
  }

  override fun onPause() {
    super.onPause()
    if (Preconditions.isConnected(neuroSky.device)) {
      neuroSky.stopMonitoring()
    }
  }

  private fun createNeuroSky(): NeuroSky {
    return NeuroSky(object : ExtendedDeviceMessageListener() {
      override fun onStateChange(state: State) {
        handleStateChange(state)
      }

      override fun onSignalChange(signal: Signal) {
        handleSignalChange(signal)
      }

      override fun onBrainWavesChange(brainWaves: Set<BrainWave>) {
        handleBrainWavesChange(brainWaves)
      }
    })
  }

  private fun handleStateChange(state: State) {
    if (state == State.CONNECTED) {
      neuroSky.startMonitoring()
    }

    tv_state.text = state.toString()
    Log.d(LOG_TAG, state.toString())
  }

  private fun handleSignalChange(signal: Signal) {
    when (signal) {
      Signal.ATTENTION -> tv_attention.text = getFormattedMessage("attention: %d", signal)
      Signal.MEDITATION -> tv_meditation.text = getFormattedMessage("meditation: %d", signal)
      Signal.BLINK -> tv_blink.text = getFormattedMessage("blink: %d", signal)
    }

    Log.d(LOG_TAG, String.format("%s: %d", signal.toString(), signal.value))
  }

  private fun getFormattedMessage(
    messageFormat: String,
    signal: Signal
  ): String {
    return String.format(Locale.getDefault(), messageFormat, signal.value)
  }

  private fun handleBrainWavesChange(brainWaves: Set<BrainWave>) {
    for (brainWave in brainWaves) {
      Log.d(LOG_TAG, String.format("%s: %d", brainWave.toString(), brainWave.value))
    }
  }

  @OnClick(R.id.btn_connect) internal fun connect() {
    try {
      neuroSky.connect()
    } catch (e: BluetoothNotEnabledException) {
      Toast.makeText(this, e.message, Toast.LENGTH_SHORT)
          .show()
      Log.d(LOG_TAG, e.message)
    }

  }

  @OnClick(R.id.btn_disconnect) internal fun disconnect() {
    neuroSky.disconnect()
  }

  @OnClick(R.id.btn_start_monitoring) internal fun startMonitoring() {
    neuroSky.startMonitoring()
  }

  @OnClick(R.id.btn_stop_monitoring) internal fun stopMonitoring() {
    neuroSky.stopMonitoring()
  }
}
