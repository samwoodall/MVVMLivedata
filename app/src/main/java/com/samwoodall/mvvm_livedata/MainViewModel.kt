package com.samwoodall.mvvm_livedata

import androidx.lifecycle.*

class MainViewModel(private val repo: Repository = Repository(), signInRepository: SignInRepository = SignInRepository()) : ViewModel(),
    LifecycleObserver {
    private val viewModelFlatMap = Transformations.switchMap(signInRepository.getOauthToken()) { repo.getData(it) }
    private val viewModelMap = Transformations.map(repo.getData("")) { it.copy(userName = "James") }
    private val viewModelZip = repo.getData("").combineAndCompute(repo.getData("")) { a, b -> a to b}

    private val mainViewModelData: MediatorLiveData<MainViewModelData> = MediatorLiveData<MainViewModelData>().apply {
        value = MainViewModelData.Loading
    }

    fun getMainViewModel(): LiveData<MainViewModelData> = mainViewModelData

    init {
        mainViewModelData.addSource(viewModelFlatMap) {userData ->
            mainViewModelData.value = MainViewModelData.Complete(userData.userName, userData.userAge, "${userData.userName} is ${userData.userAge}")
        }

        mainViewModelData.addSource(viewModelZip) {(user1, user2) ->
            mainViewModelData.value = MainViewModelData.Complete(user1.userName, user2.userAge, "${user1.userName} is ${user2.userAge}")
        }

        mainViewModelData.addSource(viewModelMap) { userData ->
            mainViewModelData.value = MainViewModelData.Complete(userData.userName, userData.userAge, "${userData.userName} is ${userData.userAge}")
        }
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