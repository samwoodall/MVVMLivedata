package com.samwoodall.mvvm_livedata

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.*
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

    private fun loading() {}
    private fun complete(complete: MainViewModelData.Complete) {
        hello.text = complete.userDesc
    }
}

class MainViewModel(private val repo: Repository = Repository(), signInRepository: SignInRepository = SignInRepository()) : ViewModel(), LifecycleObserver {
    private val viewModelResultObserver: Observer<UserData> = Observer { transformData(it) }
    private val viewModelResultsObserver: Observer<Unit> = Observer {}

    private val viewModelResult = Transformations.switchMap(signInRepository.getOauthToken()) { repo.getData(it) }
    private val viewModelResults = Transformations.map(repo.getData("")) { transformData(it) }
//    private val viewModelZip = repo.getData("").combineAndCompute(repo.getData("")) { a, b -> transformData(a.copy(userAge = b.userAge))}

    private val mainViewModelData: MutableLiveData<MainViewModelData> = MutableLiveData<MainViewModelData>().apply {
        value = MainViewModelData.Loading
    }

    fun getMainViewModel(): LiveData<MainViewModelData> = mainViewModelData

    private fun transformData(userData: UserData) {
        mainViewModelData.value = MainViewModelData.Complete(userData.userName, userData.userAge, "${userData.userName} is ${userData.userAge}")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun registerUserInfo() {
//        viewModelZip.observeForever(viewModelResultsObserver)
        viewModelResult.observeForever(viewModelResultObserver)
//        viewModelResults.observeForever(viewModelResultsObserver)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun unregisterUserInfo() {
        viewModelResult.removeObserver(viewModelResultObserver)
//        viewModelResults.removeObserver(viewModelResultsObserver)
//        viewModelZip.removeObserver(viewModelResultsObserver)
    }
}

sealed class MainViewModelData {
    object Loading : MainViewModelData()
    data class Complete(val userName: String, val userAge: Int, val userDesc: String) : MainViewModelData()
}

data class UserData(val userName: String, val userAge: Int)

class SignInRepository {
    fun getOauthToken(): LiveData<String> = MutableLiveData<String>().apply { value = "" }
}
class Repository {
    fun getData(auth: String): LiveData<UserData> = MutableLiveData<UserData>().apply { value = UserData("sam", 10) }
}
