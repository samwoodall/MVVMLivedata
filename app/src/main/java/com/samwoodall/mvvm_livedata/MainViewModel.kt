package com.samwoodall.mvvm_livedata

import androidx.lifecycle.*

class MainViewModel(private val repo: Repository = Repository(), signInRepository: SignInRepository = SignInRepository()) : ViewModel(),
    LifecycleObserver {
    private val viewModelMapObserver: Observer<UserData> = Observer { transformData(it) }
    private val viewModelObserver: Observer<Unit> = Observer {}

    private val viewModelFlatMap = Transformations.switchMap(signInRepository.getOauthToken()) { repo.getData(it) }
    private val viewModelMap = Transformations.map(repo.getData("")) { transformData(it) }
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
        viewModelZip.observeForever(viewModelObserver)
        viewModelFlatMap.observeForever(viewModelMapObserver)
        viewModelMap.observeForever(viewModelObserver)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun unregisterUserInfo() {
        viewModelFlatMap.removeObserver(viewModelMapObserver)
        viewModelMap.removeObserver(viewModelObserver)
        viewModelZip.removeObserver(viewModelObserver)
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