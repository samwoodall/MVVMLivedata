package com.samwoodall.mvvm_livedata

import androidx.lifecycle.*

class MainViewModel(private val repo: Repository = Repository(), signInRepository: SignInRepository = SignInRepository()) : ViewModel(),
    LifecycleObserver {
    private val viewModelResultObserver: Observer<UserData> = Observer { transformData(it) }
    private val viewModelResultsObserver: Observer<Unit> = Observer {}

    private val viewModelResult = Transformations.switchMap(signInRepository.getOauthToken()) { repo.getData(it) }
    private val viewModelResults = Transformations.map(repo.getData("")) { transformData(it) }
    private val viewModelZip = repo.getData("").combineAndCompute(repo.getData("")) { a, b -> transformData(a.copy(userAge = b.userAge))}

    private val mainViewModelData: MutableLiveData<MainViewModelData> = MutableLiveData<MainViewModelData>().apply {
        value = MainViewModelData.Loading
    }

    fun getMainViewModel(): LiveData<MainViewModelData> = mainViewModelData

    private fun transformData(userData: UserData) {
        mainViewModelData.value = MainViewModelData.Complete(userData.userName, userData.userAge, "${userData.userName} is ${userData.userAge}")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun registerUserInfo() {
        viewModelZip.observeForever(viewModelResultsObserver)
        viewModelResult.observeForever(viewModelResultObserver)
        viewModelResults.observeForever(viewModelResultsObserver)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun unregisterUserInfo() {
        viewModelResult.removeObserver(viewModelResultObserver)
        viewModelResults.removeObserver(viewModelResultsObserver)
        viewModelZip.removeObserver(viewModelResultsObserver)
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