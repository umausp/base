package com.usp.base

import android.content.Context
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.usp.mylocation.FetchCurrentLocation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


abstract class BaseActivity<B : ViewBinding>(val bindingFactory: (LayoutInflater) -> B) :
    AppCompatActivity() {

    abstract val rootViewModel: BaseViewModel
    val binding: B by lazy { bindingFactory(layoutInflater) }

    val locationUpdates by lazy {
        FetchCurrentLocation(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        lifecycleScope.launch {
            rootViewModel.loadingProgression.collect {
                showHideProgress(it)
            }
        }
    }

    open fun showHideProgress(showProgress: Boolean) {

    }

    open fun getCurrentLocation(location: Location) {

    }

    open fun getCurrentAddress(address: Address) {

    }

    override fun onStart() {
        super.onStart()
        showHideProgress(false)
    }

    override fun onPause() {
        super.onPause()
        locationUpdates.stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    open fun startLocationUpdates() {
        locationUpdates.stopLocationUpdates()
        locationUpdates.startLocationUpdates({
            getCurrentLocation(it)
        }, {
            it?.let { it1 -> getCurrentAddress(it1) }
        })
    }

    open fun hideKeyboard() {
        val imm: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    open fun showKeyBoard(view: View) {
        view.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}
