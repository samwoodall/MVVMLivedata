package com.samwoodall.mvvm_livedata

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        lifecycle.addObserver(viewModel)

        viewModel.getMainViewModel().observe(this,
            Observer<MainViewModelData> {
                when(it) {
                    is MainViewModelData.Loading -> loading()
                    is MainViewModelData.Complete -> complete(it)
                }
            })
    }

    override fun onResume() {
        super.onResume()
        viewModel.oneTimeCall()
    }

    private fun loading() {}
    private fun complete(complete: MainViewModelData.Complete) {
        hello.text = complete.userDesc
    }
}
